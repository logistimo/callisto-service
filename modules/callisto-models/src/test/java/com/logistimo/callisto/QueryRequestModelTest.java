package com.logistimo.callisto;

import com.logistimo.callisto.model.QueryRequestModel;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
public class QueryRequestModelTest {

  @Test
  public void queryRequestModelTest() throws CloneNotSupportedException {
    QueryRequestModel queryRequestModel = new QueryRequestModel();
    queryRequestModel.queryId = "some_query_id";
    QueryRequestModel nQueryRequestModel = queryRequestModel.clone();
    assertEquals("some_query_id", nQueryRequestModel.queryId);
  }
}