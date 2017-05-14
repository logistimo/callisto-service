package com.logistimo.callisto.service;

import com.logistimo.callisto.CallistoDataType;
import com.logistimo.callisto.DataSourceType;
import com.logistimo.callisto.QueryResults;
import com.logistimo.callisto.model.ServerConfig;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.*;

/** @author Mohan Raja */
@Service(value = "mysql")
public class MysqlService implements IDataBaseService {

  private static final Logger logger = Logger.getLogger(MysqlService.class);

  private static final List<Integer> integerDataTypes =
      new ArrayList<>(
          Arrays.asList(
              Types.BIGINT,
              Types.TINYINT,
              Types.SMALLINT,
              Types.INTEGER,
              Types.FLOAT,
              Types.DOUBLE,
              Types.NUMERIC,
              Types.DECIMAL));

  @Override
  public QueryResults fetchRows(
      ServerConfig config,
      String query,
      Map<String, String> filters,
      Optional<Integer> size,
      Optional<Integer> offset) {
    QueryResults results = new QueryResults();
    try {
      Connection con = getConnection(config);
      Statement stmt = con.createStatement();
      if (size.isPresent()) {
        query = query.concat(" LIMIT " + offset.orElse(0) + "," + size.get());
      }
      ResultSet rs = stmt.executeQuery(constructQuery(query, filters));

      ResultSetMetaData metaData = rs.getMetaData();
      int columnCount = metaData.getColumnCount();
      List<String> headings = new ArrayList<>(columnCount);
      List<CallistoDataType> dataTypes = new ArrayList<>(columnCount);
      for (int i = 1; i <= columnCount; i++) {
        headings.add(metaData.getColumnLabel(i));
        if (integerDataTypes.contains(metaData.getColumnType(i))) {
          dataTypes.add(CallistoDataType.NUMBER);
        } else {
          dataTypes.add(CallistoDataType.STRING);
        }
      }
      results.setHeadings(headings);
      results.setDataTypes(dataTypes);
      while (rs.next()) {
        List<String> row = new ArrayList<>(columnCount);
        for (int i = 1; i <= columnCount; i++) {
          row.add(rs.getString(i));
        }
        results.addRow(row);
      }
      con.close();
    } catch (Exception e) {
      logger.error("Error while fetching data from mysql using config id" + config.getId(), e);
    }
    return results;
  }

  @Override
  public DataSourceType getMetaFields() {
    DataSourceType dataSourceType = new DataSourceType();
    dataSourceType.name = "mysql";
    dataSourceType.addMeta("port");
    dataSourceType.addMeta("host");
    dataSourceType.addMeta("username");
    dataSourceType.addMeta("password");
    return dataSourceType;
  }

  private Connection getConnection(ServerConfig config)
      throws ClassNotFoundException, SQLException {
    Class.forName("com.mysql.jdbc.Driver");
    return DriverManager.getConnection(
        "jdbc:mysql://" + config.getHosts().get(0) + "/" + config.getSchema(),
        config.getUsername(),
        config.getPassword());
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
