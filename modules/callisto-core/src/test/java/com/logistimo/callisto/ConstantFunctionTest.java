package com.logistimo.callisto;

import com.logistimo.callisto.exception.CallistoException;
import com.logistimo.callisto.function.ConstantFunction;
import com.logistimo.callisto.function.FunctionParam;
import com.logistimo.callisto.model.ConstantText;
import com.logistimo.callisto.model.QueryRequestModel;
import com.logistimo.callisto.service.IConstantService;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
public class ConstantFunctionTest {

  private final String CONSTANT_1 = "some constant";

  ConstantFunction constantFunction;
  IConstantService constantService;

  @Before
  public void setUp() {
    constantFunction = new ConstantFunction();
    constantService = Mockito.mock(IConstantService.class);
    constantFunction.setConstantService(constantService);
    ConstantText constant = new ConstantText();
    constant.setConstant(CONSTANT_1);
    when(constantService.readConstant("cuser", "cons1")).thenReturn(constant);
  }

  @Test
  public void constantFunctionTest() throws CallistoException {
    FunctionParam functionParam = new FunctionParam();
    functionParam.function = "$$constant(cons1)$$";
    QueryRequestModel queryRequestModel = new QueryRequestModel();
    queryRequestModel.userId = "cuser";
    functionParam.setRequest(queryRequestModel);
    Assert.assertEquals(CONSTANT_1, constantFunction.getResult(functionParam));
  }

  @Test(expected = CallistoException.class)
  public void constantFunctionNegative1Test() throws CallistoException {
    FunctionParam functionParam = new FunctionParam();
    functionParam.function = "$$constant(cons1)$$";
    QueryRequestModel queryRequestModel = new QueryRequestModel();
    queryRequestModel.userId = "some-another-user";
    functionParam.setRequest(queryRequestModel);
    constantFunction.getResult(functionParam);
  }

  @Test(expected = CallistoException.class)
  public void constantFunctionNegative2Test() throws CallistoException {
    FunctionParam functionParam = new FunctionParam();
    functionParam.function = "$$constant(cons2)$$";
    QueryRequestModel queryRequestModel = new QueryRequestModel();
    queryRequestModel.userId = "cuser";
    functionParam.setRequest(queryRequestModel);
    constantFunction.getResult(functionParam);
  }
}