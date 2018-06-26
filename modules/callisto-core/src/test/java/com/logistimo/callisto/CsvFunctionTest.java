package com.logistimo.callisto;

import com.logistimo.callisto.exception.CallistoException;
import com.logistimo.callisto.function.CsvFunction;
import com.logistimo.callisto.function.FunctionParam;
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
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
public class CsvFunctionTest {

  private CsvFunction csvFunction;
  private IQueryService queryService;

  @Before
  public void setUp() throws CallistoException {
    csvFunction = new CsvFunction();
    queryService = Mockito.mock(IQueryService.class);
    csvFunction.setQueryService(queryService);
  }

  @Test
  public void csvFunctionTestNumbers1() throws CallistoException {
    FunctionParam param = new FunctionParam();
    param.function = "$$csv(query_id1,10,1)$$";
    param.setQueryRequestModel(new QueryRequestModel());
    QueryResults queryResults = generateQueryResultsModel("6");
    queryResults.setDataTypes(Arrays.asList(CallistoDataType.NUMBER, CallistoDataType.STRING,
        CallistoDataType.NUMBER));
    when(queryService.readData(argThat(new QueryRequestModelWithQueryId("query_id1")))).thenReturn(
        queryResults);
    String result = csvFunction.getCSV(param);
    assertEquals("6", result);
  }


  @Test
  public void csvFunctionTestNumbers2() throws CallistoException {
    FunctionParam param = new FunctionParam();
    param.function = "$$csv(query_id1,10,1)$$";
    param.setQueryRequestModel(new QueryRequestModel());
    QueryResults queryResults = generateQueryResultsModel("1","132","323");
    queryResults.setDataTypes(Arrays.asList(CallistoDataType.NUMBER, CallistoDataType.STRING,
        CallistoDataType.NUMBER));
    when(queryService.readData(argThat(new QueryRequestModelWithQueryId("query_id1")))).thenReturn(
        queryResults);
    String result = csvFunction.getCSV(param);
    assertEquals("1,132,323", result);
  }

  @Test
  public void csvFunctionTestStrings() throws CallistoException {
    FunctionParam param = new FunctionParam();
    param.function = "$$csv(query_id1,10,1)$$";
    param.setQueryRequestModel(new QueryRequestModel());

    QueryResults queryResults = generateQueryResultsModel("jhwf32#1!","Jf3of!","(3uf31)*/");
    queryResults.setDataTypes(Arrays.asList(CallistoDataType.STRING, CallistoDataType.STRING,
        CallistoDataType.NUMBER));
    when(queryService.readData(argThat(new QueryRequestModelWithQueryId("query_id1")))).thenReturn(
        queryResults);
    String result = csvFunction.getCSV(param);
    assertEquals("'jhwf32#1!','Jf3of!','(3uf31)*/'", result);
  }

  private QueryResults generateQueryResultsModel(String ... results) {
    QueryResults queryResults = new QueryResults();
    queryResults.setHeadings(Arrays.asList("h1", "h2", "h3"));
    for(String result : results) {
      queryResults.addRow(Collections.singletonList(result));
    }
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