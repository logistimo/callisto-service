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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.logistimo.callisto.exception.CallistoException;
import com.logistimo.callisto.function.FunctionParam;
import com.logistimo.callisto.function.FunctionUtil;
import com.logistimo.callisto.model.QueryRequestModel;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

/**
 * Created by chandrakant on 19/05/17.
 */
@Component
public class ResultManager {

  private static final Logger logger = Logger.getLogger(ResultManager.class);

  private FunctionManager functionManager;

  public static final BinaryOperator<String> linkedHashMapMerger = (u, v) -> {
    throw new IllegalStateException(String.format("Duplicate key %s", u));
  };

  /**
   * @param request          QueryRequestModel by user
   * @param rs               QueryResults returned by running the query
   * @param derivedColumnMap Map of derived column names and values
   * @return Derived QueryResults using original QueryResults and derivedColumnMap
   */
  public QueryResults getDesiredResult(
      QueryRequestModel request,
      QueryResults rs,
      LinkedHashMap<String, String> derivedColumnMap)
      throws CallistoException {
    List<String> headings = rs.getHeadings();
    if (derivedColumnMap == null || derivedColumnMap.isEmpty()) {
      return rs;
    }
    derivedColumnMap =
        derivedColumnMap.entrySet().stream().collect(Collectors
            .toMap(Map.Entry::getKey, e -> e.getValue().replaceAll("\n", "").replaceAll("\t", ""),
                linkedHashMapMerger, LinkedHashMap::new));
    //TODO: mechanism to identify which column is for rowHeadings,
    QueryResults results = fillResult(rs, request.rowHeadings, 0);
    QueryResults derivedResults = new QueryResults();
    derivedResults.setHeadings(new ArrayList<>(derivedColumnMap.keySet()));
    derivedResults.setRowHeadings(results.getRowHeadings());
    if (results.getHeadings() != null && results.getRows() != null) {
      Map<String, List<String>>
          functionsVarsMap =
          derivedColumnMap.entrySet().stream()
              .map(e -> Pair.of(e.getKey(), FunctionUtil.getAllFunctionsAndVariables(e.getValue())))
              .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
      for (List row : results.getRows()) {
        List<String> dRow = new ArrayList<>(derivedResults.getHeadings().size());
        for (Map.Entry<String, String> entry : derivedColumnMap.entrySet()) {
          String r =
              parseDesiredValue(request, entry.getValue(), functionsVarsMap.get(entry.getKey()),
                  headings, row);
          dRow.add(r);
        }
        derivedResults.addRow(dRow);
      }
    }
    return derivedResults;
  }

  /**
   * @param results QueryResults to be filled
   * @param index   index of rowHeading element
   * @return QueryResults after filling dummy rows for the all absent rowHeading elements
   */
  private static QueryResults fillResult(QueryResults results, List<String> rowHeadings,
                                         Integer index) {
    if (rowHeadings != null && results != null) {
      Set<String> rowHeadingsSet = new HashSet<>(rowHeadings);
      if (results.getRows() != null) {
        for (List row : results.getRows()) {
          rowHeadingsSet.remove(row.get(index));
        }
      }
      for (String heading : rowHeadings) {
        String[] nRow = new String[index + 1];
        Arrays.fill(nRow, CharacterConstants.EMPTY);
        nRow[index] = heading;
        results.addRow(Arrays.asList(nRow));
      }
    }
    return results;
  }

  /**
   * Function to parse a expression which can contain all CallistoFunctions and Variables
   *
   * @param request  QueryRequestModel by the User
   * @param str      expression to be parsed
   * @param headings result headings i.e. column names
   * @param row      result row, respective to column names
   * @return String after replacing all the CallistoFunctions and Variables
   */
  public String parseDesiredValue(
      QueryRequestModel request, String str, List<String> functionsVars, List<String> headings,
      List<String> row)
      throws CallistoException {
    int index;
    for (int i = 0; i < functionsVars.size(); i++) {
      if ((index = variableIndex(functionsVars.get(i), headings)) > -1) {
        if (index > row.size() - 1) {
          return CharacterConstants.EMPTY;
        }
        str = StringUtils.replaceOnce(str, functionsVars.get(i), row.get(index));
      } else if (FunctionUtil.isFunction(functionsVars.get(i), false)) {
        String functionType = FunctionUtil.getFunctionType(functionsVars.get(i));
        if (functionType != null) {
          ICallistoFunction function = functionManager.getFunction(functionType);
          if (function == null) {
            throw new CallistoException("Q001", functionsVars.get(i));
          }
          FunctionParam
              functionParam =
              new FunctionParam(request, headings, row, functionsVars.get(i));
          str =
              StringUtils.replaceOnce(str, functionsVars.get(i), function.getResult(functionParam));
        } else {
          throw new CallistoException("Q001", functionsVars.get(i));
        }
      }
    }
    str = StringUtils.replace(str, CharacterConstants.DOUBLE_QUOTE, CharacterConstants.EMPTY);
    str = StringUtils.replace(str, CharacterConstants.SINGLE_QUOTE, CharacterConstants.EMPTY);
    return str;
  }

  /**
   * @param val      desired value to be parsed
   * @param headings original result headings
   * @return checks if val is only a variable i.e. $xyz, and return index of variable in heading,
   * otherwise -1
   */
  public static int variableIndex(String val, List<String> headings) {
    if (headings != null
        && val.startsWith(String.valueOf(CharacterConstants.DOLLAR))
        && !val.contains(CharacterConstants.FN_ENCLOSE)) {
      for (int i = 0; i < headings.size(); i++) {
        String column = headings.get(i);
        if (column.equalsIgnoreCase(val.substring(1))) {
          return i;
        }
      }
      logger
          .error("Variable " + val + " not found in heading of QueryResult " + headings.toString());
    }
    return -1;
  }

  /**
   * function to parse DerivedColumn map from a String and add the existing columns as they are
   * @param results original QueryResults
   * @return parsed Map
   */
  public LinkedHashMap<String, String> getResultFormatMap(String strToParse, QueryResults results) {
    if (results == null || results.getHeadings() == null) {
      return null;
    }
    Type type = new TypeToken<LinkedHashMap<String, String>>() {
    }.getType();
    LinkedHashMap<String, String> filterMap;
    filterMap =
        results.getHeadings().stream().map(s -> Pair.of(s, CharacterConstants.DOLLAR + s))
            .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond,
                linkedHashMapMerger, LinkedHashMap::new));
    filterMap.putAll(new Gson().fromJson(strToParse, type));
    return filterMap;
  }

  @Autowired
  public void setFunctionManager(FunctionManager functionManager) {
    this.functionManager = functionManager;
  }
}
