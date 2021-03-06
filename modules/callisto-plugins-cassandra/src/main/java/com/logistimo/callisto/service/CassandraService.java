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

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.ConsistencyLevel;
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
import com.datastax.driver.core.exceptions.SyntaxError;
import com.google.gson.Gson;
import com.logistimo.callisto.CallistoDataType;
import com.logistimo.callisto.CassandraConnection;
import com.logistimo.callisto.DataSourceType;
import com.logistimo.callisto.QueryResults;
import com.logistimo.callisto.exception.CallistoSyntaxErrorException;
import com.logistimo.callisto.model.Datastore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 *  @author Chandrakant
 */
@Service(value = "cassandra")
public class CassandraService implements IDataBaseService {

  private Map<String, CassandraConnection> dataStoreConnections = new HashMap<>();

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

  private static final Logger logger = LoggerFactory.getLogger(CassandraService.class);

  @Override
  public QueryResults fetchRows(
      Datastore config,
      String finalQuery,
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
      logger.info("Fetching cassandra results: " + finalQuery);
      logger.info("Cassandra query filters: " + filters);
      Statement statement = getStatement(finalQuery);
      statement.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
      logger.info("Query Execution started");
      ResultSet rs = iSession.execute(statement);
      ColumnDefinitions columnDefinitions = rs.getColumnDefinitions();
      results = setResultMetaData(results, columnDefinitions);
      List<String> headers = results.getHeadings();
      if (rs.iterator() != null) {
        for (Row row : rs) {
          List<String> rowVal = new ArrayList<>(headers.size());
          for (int i = 0; i < headers.size(); i++) {
            Optional<String> rowElem = getRowElement(columnDefinitions, row, i);
            if (rowElem.isPresent()) {
              rowVal.add(rowElem.get());
            } else {
              rowVal.add("");
            }
          }
          results.addRow(rowVal);
          if (size.isPresent() && results.getRows().size() >= size.get()) {
            break;
          }
        }
      }
    } catch (InvalidQueryException | SyntaxError e) {
      throw new CallistoSyntaxErrorException("Invalid cassandra query", e);
    }
    logger.info("Query Execution finished");
    return results;
  }

  private Optional<String> getRowElement(ColumnDefinitions columnDefinitions, Row row, int i) {
    if (row.getObject(i) != null) {
      switch (columnDefinitions.getType(i).getName()) {
        case MAP:
          String result =  new Gson().toJson(row.getMap(i, DataStaxUtil.DATA_TYPE_MAPPING.get(
                  columnDefinitions.getType(i).getTypeArguments().get(0).getName()),
              DataStaxUtil.DATA_TYPE_MAPPING
                  .get(columnDefinitions.getType(i).getTypeArguments().get(1).getName())));
          return Optional.of(result);
        default:
          return Optional.ofNullable(row.getObject(i).toString());
      }
    } else {
      return Optional.empty();
    }
  }

  private QueryResults setResultMetaData(QueryResults results,
                                         ColumnDefinitions columnDefinitions) {
    if (columnDefinitions == null || columnDefinitions.size() == 0) {
      return results;
    }
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

  private int getHashCode(Datastore datastore) {
    return new Gson().toJson(datastore).hashCode();
  }

  private Session getSession(Datastore datastore) {
    if (dataStoreConnections.containsKey(datastore.getId())
        && !Objects.equals(
            dataStoreConnections.get(datastore.getId()).getServerConfigHash(),
            getHashCode(datastore))) {
      connect(datastore, true);
    } else if (!dataStoreConnections.containsKey(datastore.getId())
        || dataStoreConnections.get(datastore.getId()).getSession() == null
        || dataStoreConnections.get(datastore.getId()).getSession().isClosed()) {
      connect(datastore, false);
    }
    return dataStoreConnections.get(datastore.getId()).getSession();
  }

  private void connect(Datastore datastore, boolean reconnect) {
    CassandraConnection conn =
        dataStoreConnections.containsKey(datastore.getId())
            ? dataStoreConnections.get(datastore.getId())
            : new CassandraConnection();
    dataStoreConnections.put(datastore.getId(), conn);
    try {
      if (reconnect
          || dataStoreConnections.get(datastore.getId()).getCluster() == null
          || dataStoreConnections.get(datastore.getId()).getCluster().isClosed()) {
        String username = datastore.getUsername();
        String password = datastore.getPassword();
        Cluster cluster;
        if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password)) {
          String[] hostArray = new String[datastore.getHosts().size()];
          cluster = buildCluster(datastore, username, password, hostArray);
        } else {
          String[] hostArray = new String[datastore.getHosts().size()];
          cluster = buildCluster(datastore, hostArray);
        }
        conn.setCluster(cluster);
      }
      conn.setSession(conn.getCluster().connect(datastore.getSchema()));
      conn.setServerConfigHash(getHashCode(datastore));
      Metadata metadata = dataStoreConnections.get(datastore.getId()).getCluster().getMetadata();
      logger.info(
          "Connected to cluster: "
              + metadata.getClusterName()
              + " with partitioner: "
              + metadata.getPartitioner());
    } catch (NoHostAvailableException e) {
      throw new DriverException("No host available exception", e);
    } catch (Exception e) {
      conn.setServerConfigHash(null);
      logger.error("Exception in cassandra Connection", e);
    }
  }

  protected Cluster buildCluster(
      Datastore config, String username, String password, String[] hostArray) {
    return Cluster.builder()
        .addContactPoints(config.getHosts().toArray(hostArray))
        .withPort(config.getPort())
        .withCredentials(username, password)
        .build();
  }

  protected Cluster buildCluster(Datastore config, String[] hostArray) {
    return Cluster.builder()
        .addContactPoints(config.getHosts().toArray(hostArray))
        .withPort(config.getPort())
        .build();
  }

  protected Statement getStatement(String query) {
    return new SimpleStatement(query);
  }
}
