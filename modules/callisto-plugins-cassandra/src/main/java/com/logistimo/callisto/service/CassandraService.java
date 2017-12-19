/*
 * Copyright © 2017 Logistimo.
 *
 * This file is part of Logistimo.
 *
 * Logistimo software is a mobile & web platform for supply chain management and remote temperature monitoring in
 * low-resource settings, made available under the terms of the GNU Affero General Public License (AGPL).
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 *
 * You can be released from the requirements of the license by purchasing a commercial license. To know more about
 * the commercial license, please contact us at opensource@logistimo.com
 */

package com.logistimo.callisto.service;

import com.google.gson.Gson;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.exceptions.DriverException;
import com.datastax.driver.core.exceptions.InvalidQueryException;
import com.datastax.driver.core.exceptions.NoHostAvailableException;
import com.logistimo.callisto.CallistoDataType;
import com.logistimo.callisto.DataSourceType;
import com.logistimo.callisto.QueryResults;
import com.logistimo.callisto.model.ServerConfig;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Chandrakant
 */
@Service(value = "cassandra")
public class CassandraService implements IDataBaseService {

  private static final int DEFAULT_PAGE_SIZE = 5000;

  private Cluster cluster;
  private Session session;
  private Integer serverConfigHash;
  private static final List<DataType.Name> numericDataTypes =
      new ArrayList<>(
          Arrays.asList(
              DataType.bigint().getName(),
              DataType.counter().getName(),
              DataType.decimal().getName(),
              DataType.cdouble().getName(),
              DataType.cfloat().getName(),
              DataType.cint().getName(),
              DataType.smallint().getName(),
              DataType.tinyint().getName(),
              DataType.varint().getName()));

  private static final Logger logger = Logger.getLogger(CassandraService.class);

  @Override
  public QueryResults fetchRows(
      ServerConfig config,
      String query,
      Map<String, String> filters,
      Optional<Integer> size,
      Optional<Integer> offset) {
    QueryResults results = new QueryResults();
    try {
      Session iSession = getSession(config);
      if (iSession == null) {
        logger.warn("Cassandra session is null");
        return null;
      }
      String finalQuery = constructQuery(query, filters);
      logger.info("Fetching cassandra results: " + finalQuery);
      logger.info("Cassandra query filters: " + filters);
      Statement statement = new SimpleStatement(finalQuery);
      offset = Optional.of(offset.orElse(0));
      if (size.isPresent()) {
        statement.setFetchSize(getSize(size, offset));
      }
      logger.info("Query Execution started");
      ResultSet rs = iSession.execute(statement);
      if (offset.isPresent() && offset.get() > 0) {
        skipRows(rs, statement, size, offset.get());
      }
      ColumnDefinitions columnDefinitions = rs.getColumnDefinitions();
      results = setResultMetaData(results, columnDefinitions);
      List<String> headers = results.getHeadings();
      for (Row row : rs) {
        List<String> rowVal = new ArrayList<>(headers.size());
        for (int i = 0; i < headers.size(); i++) {
          rowVal.add(getRowElement(columnDefinitions, row, i));
        }
        results.addRow(rowVal);
        if (size.isPresent() && results.getRows().size() >= size.get()) {
          break;
        }
      }
    } catch (InvalidQueryException e) {
      throw new InvalidQueryException("Invalid query exception", e);
    } catch (Exception e) {
      logger.warn("Exception in cassandra fetch", e);
    }
    logger.info("Query Execution finished");
    return results;
  }

  private String getRowElement(ColumnDefinitions columnDefinitions, Row row, int i) {
    if (row.getObject(i) != null) {
      switch (columnDefinitions.getType(i).getName()) {
        case MAP:
          return new Gson().toJson(row.getMap(i, DataStaxUtil.DATA_TYPE_MAPPING.get(
                  columnDefinitions.getType(i).getTypeArguments().get(0).getName()),
              DataStaxUtil.DATA_TYPE_MAPPING
                  .get(columnDefinitions.getType(i).getTypeArguments().get(1).getName())));
        default:
          return row.getObject(i).toString();
      }
    } else {
      return "";
    }
  }

  private QueryResults setResultMetaData(QueryResults results,
                                         ColumnDefinitions columnDefinitions) {
    List<String> headers = new ArrayList<>(columnDefinitions.size());
    List<CallistoDataType> dataTypes = new ArrayList<>(columnDefinitions.size());
    for (ColumnDefinitions.Definition definition : columnDefinitions) {
      headers.add(definition.getName());
      if (numericDataTypes.contains(definition.getType().getName())) {
        dataTypes.add(CallistoDataType.NUMBER);
      } else {
        dataTypes.add(CallistoDataType.STRING);
      }
    }
    results.setHeadings(headers);
    results.setDataTypes(dataTypes);
    return results;
  }

  private int getSize(Optional<Integer> size, Optional<Integer> offset) {
    if (offset.isPresent()) {
      if (offset.get() > DEFAULT_PAGE_SIZE) {
        return DEFAULT_PAGE_SIZE;
      } else if (offset.get() > 0) {
        return offset.get();
      }
    } else if (size.isPresent()) {
      return size.get();
    }
    return DEFAULT_PAGE_SIZE;
  }

  private ResultSet skipRows(ResultSet rs, Statement st, Optional<Integer> size, int skip) {
    while (skip-- > 0) {
      if (rs.getAvailableWithoutFetching() == 1 && size.isPresent()) {
        if (skip + size.get() < DEFAULT_PAGE_SIZE) {
          st.setFetchSize(skip + size.get());
        } else {
          st.setFetchSize(DEFAULT_PAGE_SIZE);
        }
      }
      rs.one();
    }
    return rs;
  }

  @Override
  public DataSourceType getMetaFields() {
    DataSourceType dataSourceType = new DataSourceType();
    dataSourceType.name = "cassandra";
    dataSourceType.addMeta("port");
    dataSourceType.addMeta("host");
    dataSourceType.addMeta("username");
    dataSourceType.addMeta("password");
    dataSourceType.addMeta("keyspace");
    return dataSourceType;
  }

  public Session getSession(ServerConfig serverConfig) {
    Integer sHash = new Gson().toJson(serverConfig).hashCode();
    if (!Objects.equals(serverConfigHash, sHash)) {
      connect(serverConfig, true);
    } else if (session == null || session.isClosed()) {
      connect(serverConfig, false);
    }
    return session;
  }

  private void connect(ServerConfig config, boolean reconnect) {
    try {
      if (cluster == null || cluster.isClosed() || reconnect) {
        String username = config.getUsername();
        String password = config.getPassword();
        if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password)) {
          String[] hostArray = new String[config.getHosts().size()];
          cluster =
              Cluster.builder()
                  .addContactPoints(config.getHosts().toArray(hostArray))
                  .withPort(config.getPort())
                  .withCredentials(username, password)
                  .build();
        } else {
          String[] hostArray = new String[config.getHosts().size()];
          cluster =
              Cluster.builder()
                  .addContactPoints(config.getHosts().toArray(hostArray))
                  .withPort(config.getPort())
                  .build();
        }
      }
      session = cluster.connect(config.getSchema());
      serverConfigHash = new Gson().toJson(config).hashCode();
      Metadata metadata = cluster.getMetadata();
      logger.info(
          "Connected to cluster: "
              + metadata.getClusterName()
              + " with partitioner: "
              + metadata.getPartitioner());
    } catch (NoHostAvailableException e) {
      throw new DriverException("No host available exception", e);
    } catch (Exception e) {
      serverConfigHash = null;
      logger.error("Exception in cassandra Connection", e);
    }
  }

  private String constructQuery(String query, Map<String, String> filters) {
    if (filters != null && filters.size() > 0) {
      for (Map.Entry<String, String> entry : filters.entrySet()) {
        query = query.replace(entry.getKey(), entry.getValue());
      }
    }
    return query;
  }
}
