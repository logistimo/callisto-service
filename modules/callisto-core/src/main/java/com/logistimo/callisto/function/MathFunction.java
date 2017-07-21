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

import com.logistimo.callisto.CharacterConstants;
import com.logistimo.callisto.ICallistoFunction;
import com.logistimo.callisto.exception.CallistoException;
import com.logistimo.callisto.model.QueryRequestModel;
import com.logistimo.callisto.service.IConstantService;
import com.logistimo.callisto.service.IQueryService;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

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

  private static final Logger logger = Logger.getLogger(MathFunction.class);
  private static String name = "math";
  @Resource IConstantService constantService;
  @Resource IQueryService queryService;

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getResult(FunctionParam functionParam) throws CallistoException {
    return calculateExpression(
        functionParam.getRequest(),
        functionParam.function,
        functionParam.getResultHeadings(),
        functionParam.getResultRow(),
        constantService,
        queryService);
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

  public static String getParameter(String value) {
    String val = value.trim();
    int fnStart = StringUtils.indexOf(val, CharacterConstants.OPEN_BRACKET);
    int fnEnd = StringUtils.lastIndexOf(val, CharacterConstants.CLOSE_BRACKET);
    return StringUtils.substring(val, fnStart + 1, fnEnd);
  }

  /**
   * @param request         request model containing userId and filters
   * @param val             expression to be parsed, all the variables in this expressions should be numeric
   *                        otherwise returns null. Math function supports Link and ConstantText functions as variables in
   *                        the parameter.
   * @param constantService instance of IConstantService for constant function
   * @param queryService    instance of IQueryService for Link function
   * @return calculated value of the expression
   */
  public static String calculateExpression(
      QueryRequestModel request,
      String val,
      List<String> headings,
      List<String> row,
      IConstantService constantService,
      IQueryService queryService)
      throws CallistoException {
    val = val.replaceAll("\\s+", "");
    if (StringUtils.countMatches(val, CharacterConstants.OPEN_BRACKET)
        != StringUtils.countMatches(val, CharacterConstants.CLOSE_BRACKET)) {
      throw new CallistoException("Q101", val);
    }
    String expression = getParameter(val);
    expression = FunctionsUtil.replaceVariables(expression, headings, row);
    if (request != null) {
      expression = replaceConstants(request.userId, expression, constantService);
    }
    expression = replaceLinks(request, expression, queryService);
    String result =
        String.valueOf(
            getParenthesisValue(
                CharacterConstants.OPEN_BRACKET + expression + CharacterConstants.CLOSE_BRACKET));
    if (Objects.equals(result, "null")) {
      logger.warn("getParenthesisValue returned NULL for expression: " + expression);
      result = "0";
    }
    result = !result.contains(".") ? result : result.replaceAll("0*$", "").replaceAll("\\.$", "");
    return result;
  }

  private static String replaceLinks(
      QueryRequestModel request, String val, IQueryService queryService)
      throws CallistoException {
    try {
      int linkCount =
          StringUtils.countMatches(
              val, FunctionType.LINK.toString() + CharacterConstants.OPEN_BRACKET);
      int after = 0;
      for (int i = 0; i < linkCount; i++) {
        int sIndex =
            val.indexOf(FunctionType.LINK.toString() + CharacterConstants.OPEN_BRACKET, after);
        if (sIndex != 0 && Objects.equals(
            String.valueOf(val.charAt(sIndex - 1)), CharacterConstants.SINGLE_DOLLAR)) {
          throw new CallistoException("Q001", val);
        }
        //TODO if Link function supports '(' inside parameters in future then eIndex needs to be changed
        int eIndex = val.indexOf(CharacterConstants.CLOSE_BRACKET, after);
        after = eIndex + 1;
        val =
            StringUtils.replace(
                val,
                val.substring(sIndex - FunctionType.LINK.toString().length(), eIndex + 1),
                LinkFunction.getLink(
                    request, val.substring(sIndex, eIndex + 1), queryService));
      }
    } catch (Exception e) {
      logger.warn("Error while replacing links in expression :" + val);
    }
    return val;
  }

  private static String replaceConstants(
      String userId, String val, IConstantService constantService) throws CallistoException {
    try {
      int constantCount =
          StringUtils.countMatches(
              val, FunctionType.CONSTANT.toString() + CharacterConstants.OPEN_BRACKET);
      int after = 0;
      for (int i = 0; i < constantCount; i++) {
        int sIndex =
            val.indexOf(FunctionType.CONSTANT.toString() + CharacterConstants.OPEN_BRACKET, after);
        if (sIndex != 0 && Objects.equals(
            String.valueOf(val.charAt(sIndex - 1)), CharacterConstants.SINGLE_DOLLAR)) {
          throw new CallistoException("Q001", val);
        }
        int eIndex = val.indexOf(CharacterConstants.CLOSE_BRACKET, after);
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
      logger.warn("Error while replacing constants in expression :" + val);
    }
    return val;
  }

  public static Double getParenthesisValue(String expression) throws CallistoException {
    assert (Objects.equals(String.valueOf(expression.charAt(0)), CharacterConstants.OPEN_BRACKET)
        && Objects.equals(
        String.valueOf(expression.charAt(expression.length() - 1)),
        CharacterConstants.CLOSE_BRACKET));
    String substr = StringUtils.substring(expression, 1, expression.length() - 1);
    if (StringUtils.isNotEmpty(expression)) {
      if (!StringUtils.contains(substr, CharacterConstants.OPEN_BRACKET)
          && !StringUtils.contains(substr, CharacterConstants.CLOSE_BRACKET)) {
        return getExpressionValue(StringUtils.substring(expression, 1, expression.length() - 1));
      } else {
        StringBuilder result = new StringBuilder();
        int after = 0;
        List<Pair> parenths = getParenths(substr);
        for (int i = 0; i < parenths.size(); i++) {
          int sIndex = (int) parenths.get(i).getFirst();
          int eIndex = (int) parenths.get(i).getSecond();
          if (sIndex != -1 && eIndex != -1) {
            result.append(StringUtils.substring(substr, after, sIndex));
            result.append(getParenthesisValue(StringUtils.substring(substr, sIndex, eIndex + 1)));
            after = eIndex + 1;
          } else {
            logger.warn("Error in parenthesis expression: " + expression);
          }
        }
        result.append(StringUtils.substring(substr, after));
        return getExpressionValue(result.toString());
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
      if (Objects.equals(String.valueOf(val.charAt(i)), CharacterConstants.OPEN_BRACKET)) {
        stack.push(i);
      } else if (Objects.equals(String.valueOf(val.charAt(i)), CharacterConstants.CLOSE_BRACKET)) {
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
   * @param expr arithmetic expression to be parsed, should contain only numbers and
   *             operations, no parenthesis or variables
   * @return calculated value of expression.
   * @throws CallistoException in case Number parsing exceptions
   */
  public static Double getExpressionValue(String expr) throws CallistoException {
    assert (!StringUtils.contains(expr, CharacterConstants.OPEN_BRACKET)
        && !StringUtils.contains(expr, CharacterConstants.CLOSE_BRACKET)
        && StringUtils.isNotEmpty(expr));
    try {
      String expression = StringUtils.replace(expr, "-", "+-");
      Pair<Deque<Double>, Deque<Integer>> stackPair = getValuesAndOperatorsStack(expression);
      Deque<Double> values = stackPair.getFirst();
      Deque<Integer> operators = stackPair.getSecond();
      Deque<Double> tempValues = new ArrayDeque<>();
      Deque<Integer> tempOperators = new ArrayDeque<>();
      char[] ops = {'/', '*', '+'};
      int i = -1;
      return calculate(values, operators, tempValues, tempOperators, ops, i);
    } catch (NumberFormatException e) {
      throw new CallistoException("Q101", expr);
    } catch (Exception e) {
      logger.error("Exception in getExpressionValue()", e);
    }
    return null;
  }

  private static Double calculate(Deque<Double> values, Deque<Integer> operators,
                                  Deque<Double> tempValues, Deque<Integer> tempOperators,
                                  char[] ops, int i) {
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
      values = refillValues(values, tempValues);
      operators = refillOperators(operators, tempOperators);
      if (repeat) {
        i--;
      }
    }
    assert values.size() == 1;
    return values.pop();
  }

  private static Double getValueByOperation(int i, Double d1, Double d2) {
    if (i == 0) { //divide
      return d1 == 0 ? 0 : d2 / d1;
    } else if (i == 1) { //multiply
      return d2 * d1;
    } else if (i == 2) { //add
      return d2 + d1;
    }
    return null;
  }

  private static Deque<Integer> refillOperators(Deque<Integer> operators,
                                                Deque<Integer> tempOperators) {
    while (!tempOperators.isEmpty()) {
      operators.push(tempOperators.pop());
    }
    return operators;
  }

  private static Deque<Double> refillValues(Deque<Double> values, Deque<Double> tempValues) {
    while (!tempValues.isEmpty()) {
      values.push(tempValues.pop());
    }
    return values;
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
