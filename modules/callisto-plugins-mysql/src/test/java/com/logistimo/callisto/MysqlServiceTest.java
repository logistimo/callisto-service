/*
 * Copyright © 2018 Logistimo.
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

package com.logistimo.callisto;

import com.logistimo.callisto.model.Datastore;
import com.logistimo.callisto.service.IDataBaseService;
import com.logistimo.callisto.service.MysqlService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MysqlServiceTest {

  private IDataBaseService mysqlService;
  private Connection connection;
  private Statement statement;

  @Before
  public void setup() {
    connection = mock(Connection.class);
    statement = mock(Statement.class);
    mysqlService = new MysqlService() {
      protected Connection getConnection(Datastore config)
          throws ClassNotFoundException, SQLException {
        return connection;
      }
    };
  }

  @Test
  public void fetchRowsTest() throws SQLException {
    Map<String, String> filters = new HashMap<>();
    when(connection.createStatement()).thenReturn(statement);
    ResultSet rs = mock(ResultSet.class);
    ResultSetMetaData metadata = mock(ResultSetMetaData.class);
    when(metadata.getColumnCount()).thenReturn(10);
    when(rs.getMetaData()).thenReturn(metadata);
    when(statement.executeQuery(anyString())).thenReturn(rs);
    QueryResults
        queryResults =
        mysqlService.fetchRows(null, "select some-data from some-table", filters,
            Optional.empty(), Optional.empty());
    verify(connection, times(1)).createStatement();
    verify(statement, times(1)).executeQuery("select some-data from some-table");
    assertEquals(10, queryResults.getHeadings().size());
  }

  @Test
  public void fetchRowsDataTypeTest() throws SQLException {
    Map<String, String> filters = new HashMap<>();
    filters.put("TOKEN_F1", "v1");
    filters.put("TOKEN_F2", "v2");
    when(connection.createStatement()).thenReturn(statement);
    ResultSet rs = mock(ResultSet.class);
    ResultSetMetaData metadata = mock(ResultSetMetaData.class);
    when(metadata.getColumnCount()).thenReturn(8);
    when(metadata.getColumnType(1)).thenReturn(Types.INTEGER);
    when(metadata.getColumnType(4)).thenReturn(Types.BIGINT);
    when(metadata.getColumnType(5)).thenReturn(Types.FLOAT);
    when(metadata.getColumnType(6)).thenReturn(Types.DOUBLE);
    when(rs.getMetaData()).thenReturn(metadata);
    when(statement.executeQuery(anyString())).thenReturn(rs);
    QueryResults queryResults = mysqlService
        .fetchRows(null,
            "select some-data from some-table where sc = {{TOKEN_F1}} and scc = {{TOKEN_F2}}",
            filters, Optional.empty(), Optional.empty());
    verify(connection, times(1)).createStatement();
    verify(statement, times(1)).executeQuery(eq("select some-data from some-table where sc = v1 and"
                                                + " scc = v2"));
    assertEquals(8, queryResults.getHeadings().size());
    assertEquals(8, queryResults.getDataTypes().size());
    assertEquals(CallistoDataType.NUMBER, queryResults.getDataTypes().get(0));
    assertEquals(CallistoDataType.STRING, queryResults.getDataTypes().get(1));
    assertEquals(CallistoDataType.STRING, queryResults.getDataTypes().get(2));
    assertEquals(CallistoDataType.NUMBER, queryResults.getDataTypes().get(3));
    assertEquals(CallistoDataType.NUMBER, queryResults.getDataTypes().get(4));
    assertEquals(CallistoDataType.NUMBER, queryResults.getDataTypes().get(5));
    assertEquals(CallistoDataType.STRING, queryResults.getDataTypes().get(6));
    assertEquals(CallistoDataType.STRING, queryResults.getDataTypes().get(7));
  }

  @Test
  public void getMetaFieldsTest() {
    DataSourceType dataSourceType = mysqlService.getMetaFields();
    assertEquals("mysql", dataSourceType.name);
    assertTrue(dataSourceType.metaFields.contains("port"));
    assertTrue(dataSourceType.metaFields.contains("port"));
    assertTrue(dataSourceType.metaFields.contains("host"));
    assertTrue(dataSourceType.metaFields.contains("username"));
    assertTrue(dataSourceType.metaFields.contains("password"));
  }
}