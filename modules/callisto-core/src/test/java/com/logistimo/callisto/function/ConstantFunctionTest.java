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