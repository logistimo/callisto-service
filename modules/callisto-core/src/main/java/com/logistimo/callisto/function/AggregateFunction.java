/*
 * Copyright © 2019 Logistimo.
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
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("aggr")
public class AggregateFunction implements ICallistoFunction {

  @Autowired
  @Qualifier("math")
  private ICallistoFunction mathFunction;

  @Override
  public String getName() {
    return "aggr";
  }

  @Override
  public String getResult(FunctionParam functionParam) throws CallistoException {
    String[] params = getParameter(functionParam.function);
    String sortByColumn = params[0];
    String arithmeticExpression = params[1];
    List<String> columnsInArithmeticExpression =
        FunctionUtil.getAllVariables(arithmeticExpression, AppConstants.DOLLAR).stream()
            .map(variable -> StringUtils.substring(variable, 1))
            .collect(Collectors.toList());
    List<String> rowsCopySortedByColumn =
        functionParam.getRowsCopySortedByColumn(
            sortByColumn, new HashSet<>(columnsInArithmeticExpression));
    FunctionParam mathFunctionParam =
        new FunctionParam(
            functionParam.getRequest(),
            functionParam.getResultHeadings(),
            rowsCopySortedByColumn,
            getMathFunction(arithmeticExpression),
            functionParam.getResultSet());
    return mathFunction.getResult(mathFunctionParam);
  }

  private String[] getParameter(String value) {
    String val = value.trim();
    int fnStart = StringUtils.indexOf(val, AppConstants.OPEN_BRACKET);
    int fnEnd = StringUtils.lastIndexOf(val, AppConstants.CLOSE_BRACKET);
    String params = StringUtils.substring(val, fnStart + 1, fnEnd);
    String sortByColumn = params.substring(StringUtils.lastIndexOf(params, AppConstants.COMMA) + 1);
    String arithmeticExpression =
        params.substring(0, StringUtils.lastIndexOf(params, AppConstants.COMMA));
    return new String[] {sortByColumn, arithmeticExpression};
  }

  private String getMathFunction(String arithmeticExpression) {
    return AppConstants.FN_ENCLOSE + "math(" + arithmeticExpression + ")" + AppConstants.FN_ENCLOSE;
  }

  @Override
  public int getArgsLength() {
    return 2;
  }

  @Override
  public int getMinArgsLength() {
    return 2;
  }

  @Override
  public int getMaxArgLength() {
    return 2;
  }
}
