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

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.Statement;
import com.logistimo.callisto.model.Datastore;
import com.logistimo.callisto.service.CassandraService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CassandraPluginTest {

  public static final String DEFAULT_SCHEMA = "some-schema";
  @Mock
  Cluster cluster;

  @Mock
  Session session;

  @Mock
  Logger logger;

  private ResultSet rs;
  private SimpleStatement statement;
  private String query;

  private CassandraService cassandraService = new CassandraService() {
    protected Cluster buildCluster(Datastore config, String username, String password,
                                   String[] hostArray) {
      return cluster;
    }

    protected Cluster buildCluster(Datastore config, String[] hostArray) {
      return cluster;
    }

    protected Statement getStatement(String _query) {
      query = _query;
      return statement;
    }
  };

  @Before
  public void setup() throws Exception {
    when(session.isClosed()).thenReturn(false);
    when(cluster.connect(any())).thenReturn(session);
    when(cluster.getMetadata()).thenReturn(mock(Metadata.class));
    rs = mock(ResultSet.class);
    statement = mock(SimpleStatement.class);
    when(rs.iterator()).thenReturn(null);
    when(rs.getColumnDefinitions()).thenReturn(mock(ColumnDefinitions.class));
    when(session.execute(any(Statement.class))).thenReturn(rs);
    when(cluster.connect("some-schema")).thenReturn(session);
  }

  @Test
  public void connectionTest() {
    Datastore datastore = new Datastore();
    datastore.setId("id1");
    datastore.setName("some-cassandra");
    datastore.setType("cassandra");
    datastore.setHosts(Collections.singletonList("localhost"));
    datastore.setPort(27017);
    datastore.setSchema("some-schema");
    when(cluster.connect("some-schema")).thenReturn(session);
    cassandraService.fetchRows(datastore, null, null, Optional.empty(), Optional.empty());
    verify(cluster, times(1)).connect("some-schema");

    reset(cluster);
    when(cluster.connect("some-schema")).thenReturn(session);
    cassandraService.fetchRows(datastore, null, null, Optional.empty(), Optional.empty());
    verify(cluster, never()).connect(any());

    datastore.setSchema("some-other-schema");
    reset(cluster);
    when(cluster.connect("some-other-schema")).thenReturn(session);
    cassandraService.fetchRows(datastore, null, null, Optional.empty(), Optional.empty());
    verify(cluster, times(1)).connect("some-other-schema");
  }

  @Test
  public void resultOffset1Test() {
    Datastore datastore = new Datastore();
    datastore.setId("id1");
    datastore.setName("some-cassandra");
    datastore.setType("cassandra");
    datastore.setHosts(Collections.singletonList("localhost"));
    datastore.setPort(27017);
    datastore.setSchema("some-schema");
    cassandraService.fetchRows(datastore, null, null, Optional.empty(), Optional.of(15));
    verify(rs, times(15)).one();
  }

  @Test
  public void resultOffset2Test() {
    Datastore datastore = new Datastore();
    datastore.setId("id1");
    datastore.setName("some-cassandra");
    datastore.setType("cassandra");
    datastore.setHosts(Collections.singletonList("localhost"));
    datastore.setPort(27017);
    datastore.setSchema(DEFAULT_SCHEMA);
    cassandraService.fetchRows(datastore, null, null, Optional.of(50), Optional.of(4000));
    verify(rs, times(4000)).one();
    verify(statement, times(1)).setFetchSize(4000);
  }

  @Test
  public void resultOffset3Test() {
    Datastore datastore = new Datastore();
    datastore.setId("id1");
    datastore.setName("some-cassandra");
    datastore.setType("cassandra");
    datastore.setHosts(Collections.singletonList("localhost"));
    datastore.setPort(27017);
    datastore.setSchema("some-schema");
    cassandraService.fetchRows(datastore, null, null, Optional.empty(), Optional.of(9500));
    verify(rs, times(9500)).one();
  }

  @Test
  public void constructQueryTest() throws Exception {
    Datastore datastore = new Datastore();
    datastore.setId("id1");
    datastore.setName("some-cassandra");
    datastore.setType("cassandra");
    datastore.setHosts(Collections.singletonList("localhost"));
    datastore.setPort(27017);
    datastore.setSchema("some-schema");
    String query = "select this, that from some-table where other in (439843) and "
                   + "some-other in 'some-value'";
    Map<String, String> filters = new HashMap<>();
    cassandraService.fetchRows(datastore, query, filters, Optional.empty(), Optional.of(9500));
    assertEquals("select this, that from some-table where other in (439843) and some-other in "
                 + "'some-value'", this.query);
    verify(rs, times(9500)).one();
  }

  @Test
  public void cassandraMetadataTest() {
    DataSourceType dataSourceType = cassandraService.getMetaFields();
    assertEquals("cassandra", dataSourceType.name);
    dataSourceType.metaFields.contains("port");
    dataSourceType.metaFields.contains("host");
    dataSourceType.metaFields.contains("username");
    dataSourceType.metaFields.contains("password");
    dataSourceType.metaFields.contains("keyspace");
  }

  private class StatementQueryArgMatcher implements ArgumentMatcher<Statement> {
    private String query;

    private StatementQueryArgMatcher(String query) {
      this.query = query;
    }

    @Override
    public boolean matches(Statement argument) {
      return Objects.equals(statement.getQueryString(), query);
    }
  }
}