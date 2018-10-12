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

import com.logistimo.callisto.CallistoDataType;
import com.logistimo.callisto.DataSourceType;
import com.logistimo.callisto.QueryResults;
import com.logistimo.callisto.model.Datastore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** @author Mohan Raja */
@Service(value = "mysql")
public class MysqlService implements IDataBaseService {

  private static final Logger logger = LoggerFactory.getLogger(MysqlService.class);

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
      Datastore config,
      String query,
      Map<String, String> filters,
      Optional<Integer> size,
      Optional<Integer> offset) {
    QueryResults results = new QueryResults();
    ResultSet rs = null;
    try(Connection con = getConnection(config); Statement stmt = con.createStatement()) {
      if (size.isPresent()) {
        query = query.concat(" LIMIT " + offset.orElse(0) + "," + size.get());
      }
      String finalQuery = constructQuery(query, filters);
      logger.info("Fetching mysql results: " + finalQuery);
      rs = stmt.executeQuery(finalQuery);
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
    } catch (Exception e) {
      logger.error("Error while fetching data from mysql using config id" + config.getId(), e);
    } finally{
      try{
        if(rs != null){
          rs.close();
        }
      } catch (SQLException e) {
        logger.warn("Exception in closing SQL resultset", e);
      }
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

  private Connection getConnection(Datastore config)
      throws ClassNotFoundException, SQLException {
    Class.forName("com.mysql.jdbc.Driver");
    return DriverManager.getConnection(
        "jdbc:mysql://" + config.getHosts().get(0) + "/" + config.getSchema(),
        config.getUsername(),
        config.getPassword());
  }

  private String constructQuery(String query, Map<String, String> filters) {
    if (filters != null && filters.size() > 0) {
      for (Map.Entry<String, String> entry : filters.entrySet()) {
        String placeholder = "{{" + entry.getKey() + "}}";
        query = query.replace(placeholder, entry.getValue());
      }
    }
    return query;
  }
}
