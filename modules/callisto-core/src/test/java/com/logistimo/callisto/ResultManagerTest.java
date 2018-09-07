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

package com.logistimo.callisto;

import com.logistimo.callisto.exception.CallistoException;
import com.logistimo.callisto.function.BottomxFunction;
import com.logistimo.callisto.function.FunctionUtil;
import com.logistimo.callisto.function.MathFunction;
import com.logistimo.callisto.function.TopxFunction;
import com.logistimo.callisto.model.QueryRequestModel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/** Created by chandrakant on 26/05/17. */
@RunWith(SpringJUnit4ClassRunner.class)
public class ResultManagerTest {

  @Mock
  FunctionManager functionManager;

  @InjectMocks
  ResultManager resultManager;

  MathFunction mathFunction;
  BottomxFunction bottomxFunction;
  TopxFunction topxFunction;

  @Before
  public void setUp() {
    mathFunction = new MathFunction();
    bottomxFunction = new BottomxFunction();
    topxFunction = new TopxFunction();
  }

  @Test
  public void getDerivedResultTest() throws CallistoException {
    QueryRequestModel request = new QueryRequestModel();
    request.filters = new HashMap<>();
    request.filters.put("TOKEN_SIZE", "5");
    request.filters.put("TOKEN_OFFSET", "4");
    QueryResults results = new QueryResults();
    List<String> headings = Arrays.asList("abc", "def", "pqr", "mapc");
    results.setHeadings(headings);
    when(functionManager.getFunction("math")).thenReturn(mathFunction);
    when(functionManager.getFunction("bottomx")).thenReturn(bottomxFunction);
    when(functionManager.getFunction("topx")).thenReturn(topxFunction);
    results.addRow(Arrays.asList("result of abc", "125", "250",
        "{\"key1\":23,\"key2\":10,\"key3\":8,\"key4\":2,\"key5\":25,\"key6\":12,\"key7\":14,\"key8\":17,\"key9\":20}"));
    results.addRow(Arrays.asList("another result of abc", "49", "123",
        "{\"key1\":23,\"key2\":10,\"key3\":8,\"key4\":2,\"key5\":25,\"key6\":12,\"key7\":14,\"key8\":17,\"key9\":20}"));
    results.addRow(Arrays.asList("seriously?", "24", "340",
        "{\"key1\":23,\"key2\":10,\"key3\":8,\"key4\":2,\"key5\":25,\"key6\":12,\"key7\":14,\"key8\":17,\"key9\":20}"));
    LinkedHashMap<String, String> desiredResultFormat = new LinkedHashMap<>();
    desiredResultFormat.put("Display format of abc", "$abc $def $$math(100/(2.5*2))$$");
    desiredResultFormat.put("Modified 2nd column", "$$math($pqr/$def)$$");
    desiredResultFormat.put("Modified bottom map", "$$bottomx($mapc,TOKEN_SIZE,TOKEN_OFFSET)$$");
    desiredResultFormat.put("Modified top map", "$$topx($mapc,TOKEN_SIZE,TOKEN_OFFSET)$$");
    QueryResults newResult = resultManager.getDesiredResult(request, results, desiredResultFormat);
    assertEquals("result of abc 125 20", newResult.getRows().get(0).get(0));
    assertEquals("2", newResult.getRows().get(0).get(1));
    assertEquals("{\"key7\":14,\"key8\":17,\"key9\":20,\"key1\":23,\"key5\":25}", newResult.getRows().get(0).get(2));
    assertEquals("{\"key7\":14,\"key6\":12,\"key2\":10,\"key3\":8,\"key4\":2}", newResult.getRows().get(0).get(3));
  }


  @Test
  public void getVariablesAndFunctionsTest() throws CallistoException {
    String text = "$did | $abc | $hello+$$math( 2*$did)$$ |$abc ";
    List list = FunctionUtil.getAllFunctionsAndVariables(text);
    assertEquals(5, list.size());
    assertEquals("$did", list.get(0));
    assertEquals("$abc", list.get(1));
    assertEquals("$hello", list.get(2));
    assertEquals("$$math( 2*$did)$$", list.get(3));
    assertEquals("$abc", list.get(4));
  }

}
