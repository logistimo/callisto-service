package com.logistimo.callisto.service;

import com.datastax.driver.core.*;
import com.datastax.driver.core.exceptions.DriverException;
import com.datastax.driver.core.exceptions.InvalidQueryException;
import com.datastax.driver.core.exceptions.NoHostAvailableException;
import com.google.gson.Gson;
import com.logistimo.callisto.CallistoDataType;
import com.logistimo.callisto.DataSourceType;
import com.logistimo.callisto.QueryResults;
import com.logistimo.callisto.model.ServerConfig;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

/** @author Chandrakant */
@Service(value = "cassandra")
public class CassandraService implements IDataBaseService {

  private static final int DEFAULT_PAGE_SIZE = 5000;

  private Cluster cluster;
  private Session session;
  private Integer serverConfigHash;
  private static final List<DataType.Name> integerDataTypes =
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
      Session session = getSession(config);
      if(session==null){
          return null;
      }
      query = constructQuery(query, filters);
      logger.info("Fetching cassandra results: " + query + filters);
      Statement statement = new SimpleStatement(query);
      offset = Optional.of(offset.orElse(0));
      if (size.isPresent()) {
        if (offset.get() > DEFAULT_PAGE_SIZE) {
          statement.setFetchSize(DEFAULT_PAGE_SIZE);
        } else if (offset.get() > 0) {
          statement.setFetchSize(offset.get());
        } else {
          statement.setFetchSize(size.get());
        }
      }
      logger.info("Query Execution started");
      ResultSet rs = session.execute(statement);
      if (offset.get() > 0) {
        skipRows(rs, statement, size, offset.get());
      }
      List<String> headers;
      List<CallistoDataType> dataTypes;
      ColumnDefinitions columnDefinitions = rs.getColumnDefinitions();
      headers = new ArrayList<>(columnDefinitions.size());
      dataTypes = new ArrayList<>(columnDefinitions.size());
      for (ColumnDefinitions.Definition definition : columnDefinitions) {
        headers.add(definition.getName());
        if (integerDataTypes.contains(definition.getType().getName())) {
          dataTypes.add(CallistoDataType.NUMBER);
        } else {
          dataTypes.add(CallistoDataType.STRING);
        }
      }
      results.setHeadings(headers);
      results.setDataTypes(dataTypes);
      for (Row row : rs) {
        List<String> rowVal = new ArrayList<>(headers.size());
        for (int i = 0; i < headers.size(); i++) {
          if (row.getObject(i) != null) {
            switch(columnDefinitions.getType(i).getName()){
              case MAP:
                rowVal.add(new Gson().toJson(row.getMap(i, String.class, BigDecimal.class)));
                break;
              default:
                rowVal.add(row.getObject(i).toString());
            }
          } else {
            rowVal.add("");
          }
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
    int sHash = new Gson().toJson(serverConfig).hashCode();
    if (session == null || session.isClosed() || sHash != serverConfigHash) {
      connect(serverConfig);
    }
    return session;
  }

  private void connect(ServerConfig config) {
    try {
      if ((cluster == null || cluster.isClosed())) {
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
                  //.withCredentials(config.getUsername().trim(),config.getPassword().trim())
                  .withPort(config.getPort())
                  .build();

        }
        session = cluster.connect(config.getSchema());
        serverConfigHash = new Gson().toJson(config).hashCode();
      }
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
      for (String token : filters.keySet()) {
        query = query.replace(token, filters.get(token));
      }
    }
    return query;
  }
}
