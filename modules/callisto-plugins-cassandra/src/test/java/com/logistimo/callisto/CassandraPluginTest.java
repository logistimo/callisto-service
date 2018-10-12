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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyNew;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest({CassandraService.class, Logger.class, Cluster.class, SimpleStatement.class})
@PowerMockIgnore("javax.management.*")
public class CassandraPluginTest {

  @Mock
  Cluster cluster;

  @Mock
  Session session;

  @Mock
  Logger logger;

  private ResultSet rs;
  private SimpleStatement statement;

  @InjectMocks
  private CassandraService cassandraService = new CassandraService();

  @Before
  public void setup() throws Exception {
    when(session.isClosed()).thenReturn(false);
    when(cluster.connect(any())).thenReturn(session);
    when(cluster.getMetadata()).thenReturn(mock(Metadata.class));
    rs = mock(ResultSet.class);
    statement = mock(SimpleStatement.class);
    when(rs.isFullyFetched()).thenReturn(true);
    when(rs.getColumnDefinitions()).thenReturn(mock(ColumnDefinitions.class));
    when(session.execute(any(Statement.class))).thenReturn(rs);
    whenNew(SimpleStatement.class).withAnyArguments().thenReturn(statement);
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
    mockStatic(Cluster.class);
    cassandraService.fetchRows(datastore, null, null, Optional.empty(), Optional.empty());
    verifyStatic(times(1));
    Cluster.builder();
    cassandraService.fetchRows(datastore, null, null, Optional.empty(), Optional.empty());
    mockStatic(Cluster.class);
    verifyStatic(never());
    Cluster.builder();
    mockStatic(Cluster.class);
    datastore.setHosts(Collections.singletonList("127.0.0.1"));
    cassandraService.fetchRows(datastore, null, null, Optional.empty(), Optional.empty());
    verifyStatic(times(1));
    Cluster.builder();
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
    PowerMockito.mockStatic(Cluster.class);
    PowerMockito.doThrow(new RuntimeException()).when(Cluster.class);
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
    datastore.setSchema("some-schema");
    PowerMockito.mockStatic(Cluster.class);
    PowerMockito.doThrow(new RuntimeException()).when(Cluster.class);
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
    PowerMockito.mockStatic(Cluster.class);
    PowerMockito.doThrow(new RuntimeException()).when(Cluster.class);
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
    PowerMockito.mockStatic(Cluster.class);
    PowerMockito.doThrow(new RuntimeException()).when(Cluster.class);
    String query = "select this, that from some-table where other in ({{I_DONT_KNOW}}) and "
                   + "some-other in {{I_KNOW}}";
    Map<String, String> filters = new HashMap<>();
    filters.put("I_DONT_KNOW", "439843");
    filters.put("I_KNOW", "'some-value'");
    cassandraService.fetchRows(datastore, query, filters, Optional.empty(), Optional.of(9500));
    verifyNew(SimpleStatement.class).withArguments("select this, that from some-table where other in (439843) and some-other in 'some-value'");
    verify(rs, times(9500)).one();
  }
}