package com.logistimo.callisto;

import com.logistimo.callisto.exception.CallistoException;
import com.logistimo.callisto.function.FunctionParam;
import com.logistimo.callisto.function.LinkFunction;
import com.logistimo.callisto.model.QueryRequestModel;
import com.logistimo.callisto.service.IQueryService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
public class LinkFunctionTest {

  LinkFunction linkFunction;
  IQueryService queryService;

  @Before
  public void setUp() {
    queryService = Mockito.mock(IQueryService.class);
    linkFunction = new LinkFunction();
    linkFunction.setQueryService(queryService);
  }

  @Test
  public void linkFunctionTestInit() throws CallistoException {
    String fun = "$$link(query_id1,{})$$";
    FunctionParam param = new FunctionParam();
    param.setQueryRequestModel(new QueryRequestModel());
    param.function = fun;
    QueryResults queryResults = generateQueryResultsModel(Arrays.asList("h1"), Arrays.asList
        (Arrays.asList("This is the expected result")));
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
    QueryResults queryResults = generateQueryResultsModel(Arrays.asList("h1"), Arrays.asList
        (Arrays.asList("This is the expected result"),Arrays.asList("This is the second result")));
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
    QueryResults queryResults = generateQueryResultsModel(Arrays.asList("h1"),
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

  class QueryRequestModelWithQueryId extends ArgumentMatcher<QueryRequestModel> {

    private String queryId;
    QueryRequestModelWithQueryId(String queryId) {
      this.queryId = queryId;
    }

    @Override
    public boolean matches(Object o) {
      QueryRequestModel queryRequestModel = (QueryRequestModel) o;
      return Objects.equals(queryId, queryRequestModel.queryId);
    }
  }
}