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

import com.logistimo.callisto.exception.DuplicateQueryIdException;
import com.logistimo.callisto.function.CsvFunction;
import com.logistimo.callisto.function.EncloseCsvFunction;
import com.logistimo.callisto.function.FunctionParam;
import com.logistimo.callisto.model.Datastore;
import com.logistimo.callisto.model.PagedResults;
import com.logistimo.callisto.model.QueryRequestModel;
import com.logistimo.callisto.model.QueryText;
import com.logistimo.callisto.repository.QueryRepository;
import com.logistimo.callisto.service.IDataBaseService;
import com.logistimo.callisto.service.IQueryService;
import com.logistimo.callisto.service.impl.DatastoreService;
import com.logistimo.callisto.service.impl.QueryService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class QueryServiceTest {

  private IQueryService queryService;

  @Mock
  private QueryRepository queryRepository;

  @Mock
  private DatastoreService datastoreService;

  @Mock
  private FunctionManager functionManager;

  @Mock
  private DataBaseCollection dataBaseCollection;

  @Before
  public void setup() {
    queryService = new QueryService(queryRepository, datastoreService);
    queryService.setFunctionManager(functionManager);
    queryService.setDatabaseCollection(dataBaseCollection);
  }

  @Test
  public void saveQueryTest() {
    QueryText queryText = new QueryText();
    queryText.setQuery("some random query");
    queryText.setQueryId("some-random-query");
    queryText.setUserId("some-user");
    when(queryRepository.findOne("some-user", "some-random-query")).thenReturn(Optional.empty());
    queryService.saveQuery(queryText);
    verify(queryRepository, times(1)).save(queryText);
  }

  @Test(expected = DuplicateQueryIdException.class)
  public void saveQueryCollisionTest() {
    QueryText queryText = new QueryText();
    queryText.setQuery("some random query");
    queryText.setQueryId("some-random-query");
    queryText.setUserId("some-user");
    when(queryRepository.findOne("some-user", "some-random-query"))
        .thenReturn(Optional.of(queryText));
    queryService.saveQuery(queryText);
  }

  @Test
  public void updateQueryTest() {
    QueryText queryText = new QueryText();
    queryText.setQuery("some random query");
    queryText.setQueryId("some-random-query");
    queryText.setUserId("some-user");
    when(queryRepository.findOne("some-user", "some-random-query")).thenReturn(Optional.empty());
    queryService.updateQuery(queryText);
    verify(queryRepository, never()).save(queryText);
  }

  @Test
  public void updateQueryCollisionTest() {
    QueryText queryText = new QueryText();
    queryText.setQuery("some random query");
    queryText.setQueryId("some-random-query");
    queryText.setUserId("some-user");
    when(queryRepository.findOne("some-user", "some-random-query"))
        .thenReturn(Optional.of(queryText));
    queryService.updateQuery(queryText);
    verify(queryRepository, times(1)).save(queryText);
  }

  @Test
  public void getAllQueryIdsTest() {
    QueryText q1 = new QueryText();
    q1.setUserId("logistimo");
    q1.setQueryId("query1");
    q1.setQuery("This is query 1");
    QueryText q2 = new QueryText();
    q2.setUserId("logistimo");
    q2.setQueryId("query2");
    q2.setQuery("This is query 2");
    QueryText q3 = new QueryText();
    q3.setUserId("logistimo");
    q3.setQueryId("query3");
    q3.setQuery("This is query 3");
    when(queryRepository.readQueryIds("logistimo")).thenReturn(
        Arrays.asList(q1, q2, q3));
    List<String> queryIds = queryService.getAllQueryIds("logistimo");
    assertEquals(3, queryIds.size());
    assertEquals("query1", queryIds.get(0));
    assertEquals("query2", queryIds.get(1));
    assertEquals("query3", queryIds.get(2));
  }

  @Test
  public void searchQueriesLikeTest() {
    String userId = "logistimo";
    String like = "que";
    Pageable pageable = PageRequest.of(0, 50);
    List<QueryText> anyList = Collections.emptyList();
    when(queryRepository.searchQueriesWithQueryId(userId, like, pageable)).thenReturn(anyList);
    when(queryRepository.getSearchQueriesCount(userId, like)).thenReturn(943L);
    PagedResults<QueryText> results = queryService.searchQueriesLike(userId, like, pageable);
    assertEquals(anyList, results.getResult());
    assertEquals(943L, results.getTotalSize());
  }

  @Test
  public void readQueryIdsTest() {
    queryService.readQueryIds("logistimo", null, PageRequest.of(0, 50));
    verify(queryRepository, times(1)).readQueryIds(eq("logistimo"), any(PageRequest.class));
    reset(queryRepository);
    queryService.readQueryIds("logistimo", null, null);
    verify(queryRepository, times(1)).readQueryIds(eq("logistimo"));
    reset(queryRepository);
    queryService.readQueryIds("logistimo", "que", null);
    verify(queryRepository, times(1)).readQueryIds(eq("logistimo"), eq("que"));
    reset(queryRepository);
    queryService.readQueryIds("logistimo", "que", PageRequest.of(0, 50));
    verify(queryRepository, times(1))
        .readQueryIds(eq("logistimo"), eq("que"), any(PageRequest.class));
  }

  @Test
  public void readDataTest() {
    QueryRequestModel request = new QueryRequestModel();
    request.userId = "logistimo";
    request.queryId = "query1";
    request.filters = new HashMap<>();
    request.filters.put("f1", "v1");
    request.filters.put("f2", "'v2'");
    QueryText queryText = new QueryText();
    queryText.setUserId("logistimo");
    queryText.setQueryId("query1");
    queryText.setQuery("select some_data from some_table");
    queryText.setDatastoreId("ds1");
    Datastore datastore = new Datastore();
    datastore.setId("ds1");
    datastore.setType("cassandra");
    IDataBaseService dataBaseService = mock(IDataBaseService.class);
    when(queryRepository.readQuery(eq("logistimo"), eq("query1"), any(PageRequest.class)))
        .thenReturn(new PageImpl<>(Collections.singletonList(queryText)));
    when(datastoreService.get(eq("logistimo"), eq("ds1"))).thenReturn(datastore);
    when(dataBaseCollection.getDataBaseService(anyString())).thenReturn(dataBaseService);
    when(dataBaseService.fetchRows(any(), anyString(), anyMap(), any(), any())).thenReturn(new
        QueryResults());
    queryService.readData(request);
    verify(dataBaseService, times(1)).fetchRows(datastore, queryText.getQuery(), request.filters,
        Optional.empty(), Optional.empty());
    verify(queryRepository, times(1)).readQuery(eq("logistimo"), eq("query1"),
        any(PageRequest.class));
    verify(datastoreService, times(1)).get(eq("logistimo"), eq("ds1"));

  }

  @Test
  public void readData2Test() {
    QueryRequestModel request = new QueryRequestModel();
    request.userId = "logistimo";
    request.filters = new HashMap<>();
    request.filters.put("f1", "v1");
    request.filters.put("f2", "'v2'");
    QueryText queryText = new QueryText();
    queryText.setUserId("logistimo");
    queryText.setQueryId("query1");
    queryText.setQuery("select some_data from some_table");
    queryText.setDatastoreId("ds1");
    request.query = queryText;
    Datastore datastore = new Datastore();
    datastore.setId("ds1");
    datastore.setType("cassandra");
    IDataBaseService dataBaseService = mock(IDataBaseService.class);
    when(datastoreService.get(eq("logistimo"), eq("ds1"))).thenReturn(datastore);
    when(dataBaseCollection.getDataBaseService(anyString())).thenReturn(dataBaseService);
    when(dataBaseService.fetchRows(any(), anyString(), anyMap(), any(), any())).thenReturn(new
        QueryResults());
    queryService.readData(request);
    verify(dataBaseService, times(1)).fetchRows(datastore, queryText.getQuery(), request.filters,
        Optional.empty(), Optional.empty());
    verify(queryRepository, never()).readQuery(anyString(), anyString(), any(PageRequest.class));
    verify(datastoreService, times(1)).get(eq("logistimo"), eq("ds1"));
  }

  @Test
  public void readDataCustomFunctionsTest() {
    QueryRequestModel request = new QueryRequestModel();
    request.userId = "logistimo";
    request.filters = new HashMap<>();
    request.filters.put("f1", "v1");
    request.filters.put("f2", "'v2'");
    QueryText queryText = new QueryText();
    queryText.setUserId("logistimo");
    queryText.setQueryId("query1");
    queryText.setQuery("select some_data from some_table where some-filter in "
                       + "($$csv({{TOKEN_F1}})$$)");
    queryText.setDatastoreId("ds1");
    request.query = queryText;
    Datastore datastore = new Datastore();
    datastore.setId("ds1");
    datastore.setType("cassandra");
    IDataBaseService dataBaseService = mock(IDataBaseService.class);
    when(datastoreService.get(eq("logistimo"), eq("ds1"))).thenReturn(datastore);
    when(dataBaseCollection.getDataBaseService(anyString())).thenReturn(dataBaseService);
    when(dataBaseService.fetchRows(any(), anyString(), anyMap(), any(), any())).thenReturn(new
        QueryResults());
    ICallistoFunction function = mock(CsvFunction.class);
    when(function.getResult(argThat(new CsvFunctionParamArgMatcher("$$csv({{TOKEN_F1}})$$"))))
        .thenReturn("d1, d2, d3, d4");
    when(functionManager.getFunction(eq("csv"))).thenReturn(function);
    queryService.readData(request);
    verify(dataBaseService, times(1)).fetchRows(eq(datastore),
        eq("select some_data from some_table where some-filter in (d1, d2, d3, d4)"),
        eq(request.filters),
        eq(Optional.empty()), eq(Optional.empty()));
    verify(queryRepository, never()).readQuery(anyString(), anyString(), any(PageRequest.class));
    verify(datastoreService, times(1)).get(eq("logistimo"), eq("ds1"));
    verify(functionManager, times(1)).getFunction(eq("csv"));
  }

  @Test
  public void readAndModifyDataTest() {
    ResultManager resultManager = mock(ResultManager.class);
    QueryRequestModel request = new QueryRequestModel();
    request.userId = "logistimo";
    request.queryId = "query3";
    request.filters = new HashMap<>();
    request.filters.put("f1", "v1");
    request.filters.put("f2", "'v2'");
    request.columnText = new HashMap<>();
    request.columnText.put("TOKEN_COLUMNS", "$$math($a + $b)$$ as math-col, $$csv(some-query-id)$$ "
                                            + "as csv-col");
    QueryText queryText = new QueryText();
    queryText.setUserId("logistimo");
    queryText.setQueryId("query3");
    queryText.setQuery("select some_data from some_table where some-filter in "
                       + "($$enclosecsv({{TOKEN_F1}})$$)");
    queryText.setDatastoreId("ds1");
    IDataBaseService dataBaseService = mock(IDataBaseService.class);
    Datastore datastore = new Datastore();
    datastore.setId("ds1");
    datastore.setType("cassandra");
    when(queryRepository.readQuery(eq("logistimo"), eq("query3"), any(PageRequest.class)))
        .thenReturn(new PageImpl<>(Collections.singletonList(queryText)));
    when(datastoreService.get(eq("logistimo"), eq("ds1"))).thenReturn(datastore);
    when(dataBaseCollection.getDataBaseService(anyString())).thenReturn(dataBaseService);
    when(dataBaseService.fetchRows(any(), anyString(), anyMap(), any(), any())).thenReturn(new
        QueryResults());
    ICallistoFunction function = mock(EncloseCsvFunction.class);
    when(function.getResult(argThat(
        new CsvFunctionParamArgMatcher("$$enclosecsv({{TOKEN_F1}})$$"))))
        .thenReturn("'d1', 'd2', 'd3', 'd4'");
    when(functionManager.getFunction(eq("enclosecsv"))).thenReturn(function);
    queryService.readAndModifyData(request, resultManager);
    verify(dataBaseService, times(1)).fetchRows(eq(datastore), eq(
            "select some_data from some_table where some-filter in "
            + "('d1', 'd2', 'd3', 'd4')"),
        eq(request.filters),
        eq(Optional.empty()), eq(Optional.empty()));
    verify(queryRepository, times(1)).readQuery(eq("logistimo"), eq("query3"),
        any(PageRequest.class));
    verify(datastoreService, times(1)).get(eq("logistimo"), eq("ds1"));
    Map<String, String> modifiedColumns = new HashMap<>();
    modifiedColumns.put("math-col", "$$math($a + $b)$$");
    modifiedColumns.put("csv-col", "$$csv(some-query-id)$$");
    verify(resultManager, times(1)).getDerivedResults(eq(request), any(),
        argThat(new MapArgMatcher(modifiedColumns)));
  }

  @Test
  public void readAndModifyDataParseColumnsTest() {
    ResultManager resultManager = mock(ResultManager.class);
    QueryRequestModel request = new QueryRequestModel();
    request.userId = "logistimo";
    request.queryId = "query3";
    request.filters = new HashMap<>();
    request.filters.put("f1", "v1");
    request.filters.put("f2", "'v2'");
    request.columnText = new HashMap<>();
    request.columnText.put("TOKEN_COLUMNS", "$$math($a + $b)$$ as math-col, $$csv(some-query-id)$$ "
                                            + "as csv-col");
    QueryText queryText = new QueryText();
    queryText.setUserId("logistimo");
    queryText.setQueryId("query3");
    queryText.setQuery("select {{TOKEN_COLUMNS}} from some_table where some-filter in "
                       + "($$enclosecsv({{TOKEN_F1}})$$)");
    queryText.setDatastoreId("ds1");
    IDataBaseService dataBaseService = mock(IDataBaseService.class);
    Datastore datastore = new Datastore();
    datastore.setId("ds1");
    datastore.setType("cassandra");
    when(queryRepository.readQuery(eq("logistimo"), eq("query3"), any(PageRequest.class)))
        .thenReturn(new PageImpl<>(Collections.singletonList(queryText)));
    when(datastoreService.get(eq("logistimo"), eq("ds1"))).thenReturn(datastore);
    when(dataBaseCollection.getDataBaseService(anyString())).thenReturn(dataBaseService);
    when(dataBaseService.fetchRows(any(), anyString(), anyMap(), any(), any())).thenReturn(new
        QueryResults());
    ICallistoFunction function = mock(EncloseCsvFunction.class);
    when(function.getResult(argThat(
        new CsvFunctionParamArgMatcher("$$enclosecsv({{TOKEN_F1}})$$"))))
        .thenReturn("'d1', 'd2', 'd3', 'd4'");
    when(functionManager.getFunction(eq("enclosecsv"))).thenReturn(function);
    queryService.readAndModifyData(request, resultManager);
    verify(dataBaseService, times(1)).fetchRows(eq(datastore), eq(
            "select a,b from some_table where some-filter in "
            + "('d1', 'd2', 'd3', 'd4')"),
        eq(request.filters),
        eq(Optional.empty()), eq(Optional.empty()));
    verify(queryRepository, times(1)).readQuery(eq("logistimo"), eq("query3"),
        any(PageRequest.class));
    verify(datastoreService, times(1)).get(eq("logistimo"), eq("ds1"));
    Map<String, String> modifiedColumns = new HashMap<>();
    modifiedColumns.put("math-col", "$$math($a + $b)$$");
    modifiedColumns.put("csv-col", "$$csv(some-query-id)$$");
    verify(resultManager, times(1)).getDerivedResults(eq(request), any(),
        argThat(new MapArgMatcher(modifiedColumns)));
  }

  private class CsvFunctionParamArgMatcher implements ArgumentMatcher<FunctionParam> {
    private String functionText;

    private CsvFunctionParamArgMatcher(String function) {
      this.functionText = function;
    }

    @Override
    public boolean matches(FunctionParam argument) {
      return Objects.equals(functionText, argument.function);
    }
  }

  private class MapArgMatcher implements ArgumentMatcher<Map<String, String>> {
    private Map modifiedColumnMap;

    private MapArgMatcher(Map<String, String> modifiedColumnMap) {
      this.modifiedColumnMap = modifiedColumnMap;
    }

    @Override
    public boolean matches(Map<String, String> argument) {
      if(argument == null) {
        return modifiedColumnMap == null;
      }
      for(Map.Entry<String, String> entry : argument.entrySet()) {
        boolean r =  modifiedColumnMap.containsKey(entry.getKey()) && modifiedColumnMap.get(entry
            .getKey()).equals(entry.getValue());
        if(!r) {
          return false;
        }
      }
      return true;
   }
  }
}