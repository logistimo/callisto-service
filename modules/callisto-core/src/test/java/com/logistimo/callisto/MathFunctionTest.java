package com.logistimo.callisto;

import com.logistimo.callisto.exception.CallistoException;
import com.logistimo.callisto.function.FunctionUtil;
import com.logistimo.callisto.function.MathFunction;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
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
    assertEquals("72.75", str);
    expr = "$$math(100*($var1/$var2)*($var3/720))$$";
    str =
        MathFunction.calculateExpression(
            null, expr, Arrays.asList(headings), Arrays.asList(row), null, null);
    assertEquals("12474.2", str);
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
}