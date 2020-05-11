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

import static org.junit.Assert.assertEquals;

import com.logistimo.callisto.AppConstants;
import com.logistimo.callisto.QueryResults;
import com.logistimo.callisto.exception.CallistoException;
import com.logistimo.callisto.model.QueryRequestModel;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MathFunctionTest {

  private MathFunction mathFunction;

  @Before
  public void init() {
    AggregateFunction aggregateFunction = new AggregateFunction();
    mathFunction = new MathFunction(null, new PreviousFunction(), aggregateFunction);
    aggregateFunction.setMathFunction(mathFunction);
  }

  @Test
  public void expressionValueTest() throws CallistoException {
    String expr = "134+42/123+498-31+93*2.3-84";
    Double ans = mathFunction.getExpressionValue(expr);
    assert ans != null;
    assertEquals(731.24, ans.doubleValue(), 0.01);
    expr = "4.42*230.4/313.4+313-38*6.4+194";
    ans = mathFunction.getExpressionValue(expr);
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
    List list = FunctionUtil.getAllVariables(expr, AppConstants.DOLLAR);
    assert (list.get(0).equals("$abc") && list.get(1).equals("$def") && list.size() == 2);
    expr = "$pqr*100+32-42+($abc-$def)/$xyz";
    list = FunctionUtil.getAllVariables(expr, AppConstants.DOLLAR);
    assertEquals("$pqr", list.get(0));
    assertEquals("$abc", list.get(1));
    assertEquals("$def", list.get(2));
    assertEquals("$xyz", list.get(3));
    assertEquals(4, list.size());
  }

  @Test
  public void getParenthesisValueTest() throws CallistoException {
    String expr = "(1+2)";
    BigDecimal d = mathFunction.getParenthesisValue(expr);
    assert d != null;
    assertEquals(3, d.doubleValue(), 0);
    expr = "(5*(2+3))";
    d = mathFunction.getParenthesisValue(expr);
    assert d != null;
    assertEquals(25, d.doubleValue(), 0);
    expr = "(12*31+31-(294/4+((224/4+32*2)/2)))";
    d = mathFunction.getParenthesisValue(expr);
    assert d != null;
    assertEquals(269.5, d.doubleValue(), 0.01);
  }

  @Test
  public void calculateExpressionTest() throws CallistoException {
    String[] headings = {"var3", "var2", "var1"};
    String[] row = {"123456", "200", "145.5"};
    String expr = "$$math(100*($var1/$var2))$$";
    String str =
        mathFunction.calculateExpression(
            null, expr, Arrays.asList(headings), Arrays.asList(row));
    assertEquals(72.75, Double.valueOf(str), 0.01);
    expr = "$$math(100*($var1/$var2)*($var3/720))$$";
    str =
        mathFunction.calculateExpression(
            null, expr, Arrays.asList(headings), Arrays.asList(row));
    assertEquals(12474.2, Double.valueOf(str), 0.1);
    expr = "$$math(100*($var1*41.2/($var3/720)))$$";
    str =
        mathFunction.calculateExpression(
            null, expr, Arrays.asList(headings), Arrays.asList(row));
    assertEquals(3496.07, Double.valueOf(str), 0.01);
    expr = "$$math(100*(($var1+41.2)/($var3/720)))$$";
    str =
        mathFunction.calculateExpression(
            null, expr, Arrays.asList(headings), Arrays.asList(row));
    assertEquals(108.88, Double.valueOf(str), 0.01);
    expr = "$$math((42.453/30*321)*(342.4*(21138.4/2493)+24))$$";
    str =
        mathFunction.calculateExpression(
            null, expr, Arrays.asList(headings), Arrays.asList(row));
    assertEquals(1329692.25, Double.valueOf(str), 0.01);
    expr = "$$math(((42.453/30*321)*(342.4*(21138.4/$var1)+24))/1000)$$";
    str =
        mathFunction.calculateExpression(
            null, expr, Arrays.asList(headings), Arrays.asList(row));
    assertEquals(22607.08, Double.valueOf(str), 0.01);
  }

  @Test
  public void getExpressionValueNumberFormatTest() throws CallistoException {
    String expr = "2 + 4 - a/4";
    Double d = mathFunction.getExpressionValue(expr);
    assert d == null;
  }

  @Test
  public void replacePrevFunctionTest() {
    QueryRequestModel model = new QueryRequestModel();
    final List<String> headings = Arrays.asList("abc", "def", "t");
    final List<String> row1 = Arrays.asList("100", "155", "2020-01");
    final List<String> row2 = Arrays.asList("200", "200", "2020-02");
    final List<String> row3 = Arrays.asList("150", "0", "2020-03");
    final List<String> row4 = Arrays.asList("100", "0", "2020-04");
    final List<String> row5 = Arrays.asList("110", "240", "2020-05");
    final List<String> row6 = Arrays.asList("60", "0", "2020-06");
    final List<String> row7 = Arrays.asList("150", "300", "2020-07");
    QueryResults resultSet = new QueryResults();
    resultSet.setHeadings(headings);
    resultSet.addRow(row1);
    resultSet.addRow(row2);
    resultSet.addRow(row3);
    resultSet.addRow(row4);
    resultSet.addRow(row5);
    resultSet.addRow(row6);
    resultSet.addRow(row7);
    String function = "$$math(100*$abc/prev(def,t))$$";
    FunctionParam param = new FunctionParam(new QueryRequestModel(), headings, row4, function,
        resultSet);
    String result = mathFunction.getResult(param);
    assertEquals("50", result);

    param = new FunctionParam(new QueryRequestModel(), headings, row3, function, resultSet);
    result = mathFunction.getResult(param);
    assertEquals("75", result);

    param = new FunctionParam(new QueryRequestModel(), headings, row6, function, resultSet);
    result = mathFunction.getResult(param);
    assertEquals("25", result);
  }

  @Test
  public void replaceAggrFunctionTest() {
    QueryRequestModel model = new QueryRequestModel();
    final List<String> headings = Arrays.asList("abc", "def", "t");
    final List<String> row1 = Arrays.asList("100", "350", "2020-01");
    final List<String> row2 = Arrays.asList("200", "30", "2020-02");
    final List<String> row3 = Arrays.asList("171", "0", "2020-03");
    final List<String> row4 = Arrays.asList("100", "20", "2020-04");
    final List<String> row5 = Arrays.asList("110", "25", "2020-05");
    final List<String> row6 = Arrays.asList("264", "15", "2020-06");
    final List<String> row7 = Arrays.asList("150", "5", "2020-07");
    QueryResults resultSet = new QueryResults();
    resultSet.setHeadings(headings);
    resultSet.addRow(row1);
    resultSet.addRow(row2);
    resultSet.addRow(row3);
    resultSet.addRow(row4);
    resultSet.addRow(row5);
    resultSet.addRow(row6);
    resultSet.addRow(row7);
    String function = "$$math(100*$abc/aggr($def,t))$$";
    FunctionParam param = new FunctionParam(new QueryRequestModel(), headings, row4, function,
        resultSet);
    String result = mathFunction.getResult(param);
    assertEquals("25", result);

    param = new FunctionParam(new QueryRequestModel(), headings, row3, function, resultSet);
    result = mathFunction.getResult(param);
    assertEquals("45", result);

    param = new FunctionParam(new QueryRequestModel(), headings, row6, function, resultSet);
    result = mathFunction.getResult(param);
    assertEquals("60", result);
  }
}
