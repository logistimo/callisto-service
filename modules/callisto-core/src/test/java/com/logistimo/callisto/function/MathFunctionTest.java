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

package com.logistimo.callisto.function;

import com.logistimo.callisto.CharacterConstants;
import com.logistimo.callisto.ICallistoFunction;
import com.logistimo.callisto.exception.CallistoException;
import com.logistimo.callisto.model.QueryRequestModel;
import com.logistimo.callisto.service.IConstantService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MathFunctionTest {

  @Test
  public void expressionValueTest() throws CallistoException {
    String expr = "134+42/123+498-31+93*2.3-84";
    Double ans = MathFunction.getExpressionValue(expr);
    assert ans != null;
    assertEquals(731.24, ans.doubleValue(), 0.01);
    expr = "4.42*230.4/313.4+313-38*6.4+194";
    ans = MathFunction.getExpressionValue(expr);
    assert ans != null;
    assertEquals(267.04, ans.doubleValue(), 0.01);
    expr = "34.45000";
    expr = MathFunction.removeTrailingZeros(expr);
    assertEquals("34.45", expr);
    expr = "34.7";
    expr = MathFunction.removeTrailingZeros(expr);
    assertEquals("34.7", expr);
    expr = "34.7000200";
    expr = MathFunction.removeTrailingZeros(expr);
    assertEquals("34.70002", expr);
  }

  @Test
  public void getAllVariablesTest() {
    String expr = "100+32-42+$abc-$def";
    List list = FunctionUtil.getAllVariables(expr, CharacterConstants.DOLLAR);
    assert (list.get(0).equals("$abc") && list.get(1).equals("$def") && list.size() == 2);
    expr = "$pqr*100+32-42+($abc-$def)/$xyz";
    list = FunctionUtil.getAllVariables(expr, CharacterConstants.DOLLAR);
    assertEquals("$pqr", list.get(0));
    assertEquals("$abc", list.get(1));
    assertEquals("$def", list.get(2));
    assertEquals("$xyz", list.get(3));
    assertEquals(4, list.size());
  }

  @Test
  public void getParenthesisValueTest() throws CallistoException {
    String expr = "(1+2)";
    Double d = MathFunction.getParenthesisValue(expr);
    assert d != null;
    assertEquals(3, d.doubleValue(), 0);
    expr = "(5*(2+3))";
    d = MathFunction.getParenthesisValue(expr);
    assert d != null;
    assertEquals(25, d.doubleValue(), 0);
    expr = "(12*31+31-(294/4+((224/4+32*2)/2)))";
    d = MathFunction.getParenthesisValue(expr);
    assert d != null;
    assertEquals(269.5, d.doubleValue(), 0.01);
  }

  @Test
  public void calculateExpressionTest() throws CallistoException {
    String[] headings = {"var3", "var2", "var1"};
    String[] row = {"123456", "200", "145.5"};
    String expr = "$$math(100*($var1/$var2))$$";
    String str =
        MathFunction.calculateExpression(
            null, expr, Arrays.asList(headings), Arrays.asList(row), null, null);
    assertEquals(72.75, Double.valueOf(str), 0.01);
    expr = "$$math(100*($var1/$var2)*($var3/720))$$";
    str =
        MathFunction.calculateExpression(
            null, expr, Arrays.asList(headings), Arrays.asList(row), null, null);
    assertEquals(12474.2, Double.valueOf(str), 0.1);
    expr = "$$math(100*($var1*41.2/($var3/720)))$$";
    str =
        MathFunction.calculateExpression(
            null, expr, Arrays.asList(headings), Arrays.asList(row), null, null);
    assertEquals(3496.07, Double.valueOf(str), 0.01);
    expr = "$$math(100*(($var1+41.2)/($var3/720)))$$";
    str =
        MathFunction.calculateExpression(
            null, expr, Arrays.asList(headings), Arrays.asList(row), null, null);
    assertEquals(108.88, Double.valueOf(str), 0.01);
    expr = "$$math((42.453/30*321)*(342.4*(21138.4/2493)+24))$$";
    str =
        MathFunction.calculateExpression(
            null, expr, Arrays.asList(headings), Arrays.asList(row), null, null);
    assertEquals(1329692.25, Double.valueOf(str), 0.01);
    expr = "$$math(((42.453/30*321)*(342.4*(21138.4/$var1)+24))/1000)$$";
    str =
        MathFunction.calculateExpression(
            null, expr, Arrays.asList(headings), Arrays.asList(row), null, null);
    assertEquals(22607.08, Double.valueOf(str), 0.01);
  }

  @Test
  public void getExpressionValueNumberFormatTest() throws CallistoException {
    String expr = "2 + 4 - a/4";
    Double d = MathFunction.getExpressionValue(expr);
    assert d == null;
  }

  @Test
  public void replaceLinksTest() {
    QueryRequestModel model = new QueryRequestModel();
    final List<String> headings = Arrays.asList("abc", "def", "ghi");
    final List<String> row = Arrays.asList("100", "34", "anthony");
    String val = "$$math(5+(3*link($ghi)))$$";
    ICallistoFunction linkFunction = mock(ICallistoFunction.class);
    when(linkFunction.getResult(argThat(new ArgumentMatcher<FunctionParam>() {
      @Override
      public boolean matches(FunctionParam argument) {
        return Objects.equals(argument.function, "$$link(anthony)$$")
            && argument.getResultHeadings() == headings
            && argument.getResultRow() == row;
      }
    }))).thenReturn("4934");
    String result = MathFunction.calculateExpression(model, val, headings, row,
        mock(IConstantService.class), linkFunction);
    assertEquals("14807", result);
  }
}