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

import com.logistimo.callisto.AppConstants;
import com.logistimo.callisto.ICallistoFunction;
import com.logistimo.callisto.exception.CallistoException;
import com.logistimo.callisto.model.QueryRequestModel;
import com.logistimo.callisto.service.IConstantService;

import java.math.BigDecimal;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

import javax.annotation.Resource;

/**
 * Created by chandrakant on 18/05/17.
 */
@Component(value = "math")
public class MathFunction implements ICallistoFunction {

  private static final Logger logger = LoggerFactory.getLogger(MathFunction.class);
  private static final String NAME = "math";
  private static final ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");

  @Resource
  IConstantService constantService;

  private ICallistoFunction linkFunction;

  private ICallistoFunction prevFunction;

  private ICallistoFunction aggregateFunction;

  private FunctionParam functionParam;

  @Autowired
  @Qualifier("link")
  public void setLinkFunction(ICallistoFunction linkFunction) {
    this.linkFunction = linkFunction;
  }

  @Autowired
  @Qualifier("prev")
  public void setPrevFunction(ICallistoFunction prevFunction) {
    this.prevFunction = prevFunction;
  }

  @Autowired
  @Qualifier("aggr")
  public void setAggregateFunction(ICallistoFunction aggregateFunction) {
    this.aggregateFunction = aggregateFunction;
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public String getResult(FunctionParam functionParam) throws CallistoException {
    this.functionParam = functionParam;
    return calculateExpression(
        functionParam.getRequest(),
        functionParam.function,
        functionParam.getResultHeadings(),
        functionParam.getResultRow());
  }

  @Override
  public int getArgsLength() {
    return 1;
  }

  @Override
  public int getMinArgsLength() {
    return -1;
  }

  @Override
  public int getMaxArgLength() {
    return -1;
  }

  private static String getParameter(String value) {
    String val = value.trim();
    int fnStart = StringUtils.indexOf(val, AppConstants.OPEN_BRACKET);
    int fnEnd = StringUtils.lastIndexOf(val, AppConstants.CLOSE_BRACKET);
    return StringUtils.substring(val, fnStart + 1, fnEnd);
  }

  /**
   * @param request request model containing userId and filters
   * @param val     expression to be parsed, all the variables in this expressions should be numeric
   *                otherwise returns null. Math function supports Link and ConstantText functions
   *                as variables in the parameter.
   * @return calculated value of the expression
   */
  String calculateExpression(
      QueryRequestModel request,
      String val,
      List<String> headings,
      List<String> row)
      throws CallistoException {
    if (StringUtils.countMatches(val, AppConstants.OPEN_BRACKET)
        != StringUtils.countMatches(val, AppConstants.CLOSE_BRACKET)) {
      throw new CallistoException("Q101", val);
    }
    String expression = getParameter(val);
    expression = replacePrevFunction(request, headings, row, expression);
    expression = replaceAggrFunction(request, headings, row, expression);
    expression = FunctionUtil.replaceVariables(expression, headings, row, "null");
    if (StringUtils.isEmpty(expression)) {
      return AppConstants.EMPTY;
    }
    if (request != null) {
      expression = replaceConstants(request.userId, expression);
    }
    expression = replaceLinks(request, headings, row, expression);
    return String.valueOf(getExpressionValueByScriptEngine(expression));
  }

  static String removeTrailingZeros(String num) {
    return !num.contains(AppConstants.DOT)
        ? num
        : num.replaceAll("0*$", AppConstants.EMPTY).replaceAll("\\.$", AppConstants.EMPTY);
  }

  private String replaceLinks(
      QueryRequestModel request,
      List<String> headings,
      List<String> row,
      final String val)
      throws CallistoException {
    String result = val;
    try {
      int linkCount =
          StringUtils.countMatches(val, FunctionType.LINK.toString() + AppConstants.OPEN_BRACKET);
      int after = 0;
      for (int i = 0; i < linkCount; i++) {
        int sIndex = val.indexOf(FunctionType.LINK.toString() + AppConstants.OPEN_BRACKET, after);
        if (sIndex != 0 && val.charAt(sIndex - 1) == AppConstants.DOLLAR) {
          throw new CallistoException("Q001", val);
        }
        // TODO if Link function supports '(' inside parameters in future then eIndex needs to be
        // changed
        int eIndex = val.indexOf(AppConstants.CLOSE_BRACKET, after);
        after = eIndex + 1;
        String functionText = val.substring(sIndex, eIndex + 1);
        FunctionParam param =
            new FunctionParam(
                request,
                headings,
                row,
                AppConstants.FN_ENCLOSE + functionText + AppConstants.FN_ENCLOSE,
                null);
        result = StringUtils.replace(val, functionText, linkFunction.getResult(param));
      }
    } catch (Exception e) {
      logger.warn("Error while replacing links in expression :" + val, e);
    }
    return result;
  }

  private String replacePrevFunction(
      QueryRequestModel request,
      List<String> headings,
      List<String> row,
      final String val)
      throws CallistoException {
    String result = val;
    try {
      int prevFunctionCount =
          StringUtils.countMatches(val, prevFunction.getName() + AppConstants.OPEN_BRACKET);
      int after = 0;
      for (int i = 0; i < prevFunctionCount; i++) {
        int sIndex = val.indexOf(prevFunction.getName() + AppConstants.OPEN_BRACKET, after);
        int eIndex = val.indexOf(AppConstants.CLOSE_BRACKET, after);
        after = eIndex + 1;
        String functionText = val.substring(sIndex, eIndex + 1);
        FunctionParam prevFunctionParam =
            new FunctionParam(
                request,
                headings,
                row,
                AppConstants.FN_ENCLOSE + functionText + AppConstants.FN_ENCLOSE,
                functionParam.getResultSet()
            );
        prevFunctionParam.setDimensions(this.functionParam.getDimensions());
        result = StringUtils.replace(val, functionText, prevFunction.getResult(prevFunctionParam));
      }
    } catch (Exception e) {
      logger.warn("Error while replacing prev function results in expression :" + val, e);
    }
    return result;
  }

  private String replaceAggrFunction(
      QueryRequestModel request,
      List<String> headings,
      List<String> row,
      final String val)
      throws CallistoException {
    String result = val;
    try {
      int aggrFunctionCount =
          StringUtils.countMatches(val, aggregateFunction.getName() + AppConstants.OPEN_BRACKET);
      int after = 0;
      for (int i = 0; i < aggrFunctionCount; i++) {
        int sIndex = val.indexOf(aggregateFunction.getName() + AppConstants.OPEN_BRACKET, after);
        int eIndex = val.indexOf(AppConstants.CLOSE_BRACKET, after);
        after = eIndex + 1;
        String functionText = val.substring(sIndex, eIndex + 1);
        FunctionParam aggrFunctionParam =
            new FunctionParam(
                request,
                headings,
                row,
                AppConstants.FN_ENCLOSE + functionText + AppConstants.FN_ENCLOSE,
                functionParam.getResultSet()
            );
        aggrFunctionParam.setDimensions(this.functionParam.getDimensions());
        result = StringUtils
            .replace(val, functionText, aggregateFunction.getResult(aggrFunctionParam));
      }
    } catch (Exception e) {
      logger.warn("Error while replacing aggr function results in expression :" + val, e);
    }
    return result;
  }

  private String replaceConstants(
      String userId, String val) throws CallistoException {
    try {
      int constantCount =
          StringUtils.countMatches(
              val, FunctionType.CONSTANT.toString() + AppConstants.OPEN_BRACKET);
      int after = 0;
      for (int i = 0; i < constantCount; i++) {
        int sIndex =
            val.indexOf(FunctionType.CONSTANT.toString() + AppConstants.OPEN_BRACKET, after);
        if (sIndex != 0 && val.charAt(sIndex - 1) == AppConstants.DOLLAR) {
          throw new CallistoException("Q001", val);
        }
        int eIndex = val.indexOf(AppConstants.CLOSE_BRACKET, after);
        after = eIndex + 1;
        val =
            StringUtils.replace(
                val,
                val.substring(sIndex - FunctionType.CONSTANT.toString().length(), eIndex + 1),
                constantService
                    .readConstant(userId, val.substring(sIndex, eIndex + 1))
                    .getConstant());
      }
    } catch (Exception e) {
      logger.warn("Error while replacing constants in expression :" + val, e);
    }
    return val;
  }

  BigDecimal getParenthesisValue(String expression) throws CallistoException {
    assert (Objects.equals(String.valueOf(expression.charAt(0)), AppConstants.OPEN_BRACKET)
        && Objects.equals(
        String.valueOf(expression.charAt(expression.length() - 1)),
        AppConstants.CLOSE_BRACKET));
    String substr = StringUtils.substring(expression, 1, expression.length() - 1);
    if (StringUtils.isNotEmpty(expression)) {
      if (!StringUtils.contains(substr, AppConstants.OPEN_BRACKET)
          && !StringUtils.contains(substr, AppConstants.CLOSE_BRACKET)) {
        return getExpressionValueByScriptEngine(
            StringUtils.substring(expression, 1, expression.length() - 1));
      } else {
        StringBuilder result = new StringBuilder();
        int after = 0;
        List<Pair> parenths = getParenths(substr);
        for (Pair parenth : parenths) {
          int sIndex = (int) parenth.getFirst();
          int eIndex = (int) parenth.getSecond();
          if (sIndex != -1 && eIndex != -1) {
            result.append(StringUtils.substring(substr, after, sIndex));
            result.append(getParenthesisValue(StringUtils.substring(substr, sIndex, eIndex + 1)));
            after = eIndex + 1;
          } else {
            logger.warn("Error in parenthesis expression: " + expression);
          }
        }
        result.append(StringUtils.substring(substr, after));
        return getExpressionValueByScriptEngine(result.toString());
      }
    }
    return null;
  }

  /**
   * @param val expression to be parsed for parenthesis
   * @return returns a List of Parenthesis as Pair of (startIndex, endIndex), outermost parenthesis
   * only and not nested ones.
   */
  public static List<Pair> getParenths(String val) {
    Deque<Integer> stack = new ArrayDeque<>();
    List<Pair> list = new ArrayList<>();
    for (int i = 0; i < val.length(); i++) {
      if (Objects.equals(String.valueOf(val.charAt(i)), AppConstants.OPEN_BRACKET)) {
        stack.push(i);
      } else if (Objects.equals(String.valueOf(val.charAt(i)), AppConstants.CLOSE_BRACKET)) {
        int temp = stack.pop();
        if (stack.isEmpty()) {
          list.add(Pair.of(temp, i));
        }
      }
    }
    return list;
  }

  /**
   * Stack based implementation for parsing a arithmetic expression. Alternatively {@link
   * javax.script.ScriptEngine} can also be used.
   *
   * @param expr arithmetic expression to be parsed, should contain only numbers and operations, no
   *             parenthesis or variables
   * @return calculated value of expression.
   * @throws CallistoException in case Number parsing exceptions
   */
  Double getExpressionValue(String expr) throws CallistoException {
    ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
    assert (!StringUtils.contains(expr, AppConstants.OPEN_BRACKET)
        && !StringUtils.contains(expr, AppConstants.CLOSE_BRACKET)
        && StringUtils.isNotEmpty(expr));
    try {
      String expression = StringUtils.replace(expr, "-", "+-");
      Pair<Deque<Double>, Deque<Integer>> stackPair = getValuesAndOperatorsStack(expression);
      Deque<Double> values = stackPair.getFirst();
      Deque<Integer> operators = stackPair.getSecond();
      return calculate(values, operators);
    } catch (NumberFormatException e) {
      logger.warn("Invalid arithmetic expression: " + expr, e);
    } catch (Exception e) {
      logger.error("Exception in getExpressionValue()", e);
    }
    return null;
  }

  private static BigDecimal getExpressionValueByScriptEngine(String expression) {
    try {
      return new BigDecimal(engine.eval(expression).toString());
    } catch (Exception e) {
      logger.error("Exception while evaluating expression: " + expression, e);
      throw new CallistoException("Invalid arithmetic expression: " + expression, e);
    }
  }

  private static Double calculate(Deque<Double> values, Deque<Integer> operators) {
    Deque<Double> tempValues = new ArrayDeque<>();
    Deque<Integer> tempOperators = new ArrayDeque<>();
    char[] ops = {'/', '*', '+'};
    int i = -1;
    while (i++ < ops.length) {
      boolean repeat = false;
      while (!operators.isEmpty()) {
        Double d1 = values.pop();
        Double d2 = values.pop();
        int operation = operators.pop();
        if (operation == ops[i]) {
          Double d = getValueByOperation(i, d1, d2);
          if (d != null) {
            tempValues.push(d);
            repeat = true;
            break;
          }
        } else {
          values.push(d2);
          tempValues.push(d1);
          tempOperators.push(operation);
        }
      }
      values = refill(values, tempValues);
      operators = refill(operators, tempOperators);
      if (repeat) {
        i--;
      }
    }
    assert values.size() == 1;
    return values.pop();
  }

  private static Double getValueByOperation(int i, Double d1, Double d2) {
    switch (i) {
      case 0: // divide
        return d1 == 0 ? 0 : d2 / d1;
      case 1: // multiply
        return d2 * d1;
      case 2: // addition
        return d2 + d1;
      default:
        return null;
    }
  }

  private static <T> Deque<T> refill(Deque<T> d1, Deque<T> d2) {
    while (!d2.isEmpty()) {
      d1.push(d2.pop());
    }
    return d1;
  }

  private static Pair<Deque<Double>, Deque<Integer>> getValuesAndOperatorsStack(String expression) {
    Deque<Double> values = new ArrayDeque<>();
    Deque<Integer> operators = new ArrayDeque<>();
    StringBuilder temp = new StringBuilder();
    for (int i = 0; i < expression.length(); i++) {
      if (expression.charAt(i) == '-') {
        temp.append('-');
      } else if (expression.charAt(i) == '+'
          || expression.charAt(i) == '*'
          || expression.charAt(i) == '/') {
        operators.push((int) expression.charAt(i));
        values.push(Double.parseDouble(temp.toString()));
        temp = new StringBuilder();
      } else {
        temp.append(expression.charAt(i));
      }
    }
    values.push(Double.parseDouble(temp.toString()));
    return Pair.of(values, operators);
  }
}
