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

package com.logistimo.callisto.function;

import com.logistimo.callisto.QueryResults;
import com.logistimo.callisto.exception.CallistoException;
import com.logistimo.callisto.model.QueryRequestModel;
import com.logistimo.callisto.service.IQueryService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LinkFunctionTest {

  @Mock
  IQueryService queryService;

  @InjectMocks
  LinkFunction linkFunction;

  @Test
  public void linkFunctionTestInit() throws CallistoException {
    String fun = "$$link(query_id1,{})$$";
    FunctionParam param = new FunctionParam();
    param.setQueryRequestModel(new QueryRequestModel());
    param.function = fun;
    QueryResults queryResults = generateQueryResultsModel(
        Collections.singletonList("h1"),
        Collections.singletonList(Collections.singletonList("This is the expected result")));
    when(queryService.readData(argThat(new QueryRequestModelWithQueryId("query_id1"))))
        .thenReturn(queryResults);
    String result = linkFunction.getResult(param);
    assertEquals("This is the expected result", result);
  }

  @Test(expected = CallistoException.class)
  public void linkFunctionTestMultipleResults() throws CallistoException {
    String fun = "$$link(query_id1,{})$$";
    FunctionParam param = new FunctionParam();
    param.setQueryRequestModel(new QueryRequestModel());
    param.function = fun;
    QueryResults queryResults = generateQueryResultsModel(
        Collections.singletonList("h1"), Arrays.asList
        (Collections.singletonList("This is the expected result"),
            Collections.singletonList("This is the second result")));
    when(queryService.readData(argThat(new QueryRequestModelWithQueryId("query_id1"))))
        .thenReturn(queryResults);
    String result = linkFunction.getResult(param);
    assertEquals("This is the expected result", result);
  }


  @Test(expected = CallistoException.class)
  public void linkFunctionTestNoResults() throws CallistoException {
    String fun = "$$link(query_id1,{})$$";
    FunctionParam param = new FunctionParam();
    param.setQueryRequestModel(new QueryRequestModel());
    param.function = fun;
    QueryResults queryResults = generateQueryResultsModel(Collections.singletonList("h1"),
        Collections.emptyList());
    when(queryService.readData(argThat(new QueryRequestModelWithQueryId("query_id1"))))
        .thenReturn(queryResults);
    String result = linkFunction.getResult(param);
    assertEquals("This is the expected result", result);
  }

  private QueryResults generateQueryResultsModel(List<String> headings, List<List<String>> rows) {
    QueryResults queryResults = new QueryResults();
    queryResults.setHeadings(headings);
    rows.forEach(queryResults::addRow);
    return queryResults;
  }

  class QueryRequestModelWithQueryId implements ArgumentMatcher<QueryRequestModel> {

    private String queryId;
    QueryRequestModelWithQueryId(String queryId) {
      this.queryId = queryId;
    }

    @Override
    public boolean matches(QueryRequestModel queryRequestModel) {
      return Objects.equals(queryId, queryRequestModel.queryId);
    }
  }
}