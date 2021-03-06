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

import java.util.HashSet;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

/** Created by chandrakant on 19/05/17. */
@Component
public class ResultManager {

  private static final Logger logger = LoggerFactory.getLogger(ResultManager.class);

  private FunctionManager functionManager;

  public static final BinaryOperator<String> linkedHashMapMerger =
      (u, v) -> {
        throw new IllegalStateException(String.format("Duplicate key %s", u));
      };

  /**
   * This method maintains the order the columns in QueryResults as provided in the derivedColumnMap
   *
   * @param request QueryRequestModel by user
   * @param rs QueryResults returned by running the query
   * @param derivedColumnMap Map of derived column names and values
   * @return Derived QueryResults using original QueryResults and derivedColumnMap
   */
  @CacheEvict(
      allEntries = true,
      value = {"links"})
  public QueryResults getDerivedResults(
      QueryRequestModel request, QueryResults rs, Map<String, String> derivedColumnMap)
      throws CallistoException {
    List<String> headings = rs.getHeadings();
    if (CollectionUtils.isEmpty(derivedColumnMap)) {
      return rs;
    }
    derivedColumnMap
        .entrySet()
        .forEach(e -> e.setValue(e.getValue().replaceAll("\n", "").replaceAll("\t", "")));
    // TODO: mechanism to identify which column is for rowHeadings,
    rs.fillResults(request.rowHeadings, 0);
    QueryResults derivedResults = new QueryResults();
    derivedResults.setHeadings(new ArrayList<>(derivedColumnMap.keySet()));
    derivedResults.setRowHeadings(rs.getRowHeadings());
    if (rs.getHeadings() != null && rs.getRows() != null) {
      Map<String, List<String>> functionsVarsMap =
          derivedColumnMap.entrySet().stream()
              .map(e -> Pair.of(e.getKey(), FunctionUtil.getAllFunctionsAndVariables(e.getValue())))
              .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
      for (List<String> row : rs.getRows()) {
        List<String> dRow = new ArrayList<>(derivedResults.getHeadings().size());
        for (Map.Entry<String, String> entry : derivedColumnMap.entrySet()) {
          String fieldValue = "";
          try {
            fieldValue =
                parseDerivedValue(
                    request,
                    entry.getValue(),
                    functionsVarsMap.get(entry.getKey()),
                    headings,
                    row,
                    rs);
          } catch (Exception e) {
            logger.error("Exception while computing derived value", e);
          }
          dRow.add(fieldValue);
        }
        derivedResults.addRow(dRow);
      }
    }
    return derivedResults;
  }

  /**
   * Function to parse a expression which can contain all CallistoFunctions and Variables
   *
   * @param request QueryRequestModel by the User
   * @param str expression to be parsed
   * @param headings result headings i.e. column names
   * @param row result row, respective to column names
   * @param resultSet
   * @return String after replacing all the CallistoFunctions and Variables
   */
  public String parseDerivedValue(
      QueryRequestModel request,
      String str,
      List<String> functionsVars,
      List<String> headings,
      List<String> row,
      QueryResults resultSet)
      throws CallistoException {
    int index;
    for (String functionsVar : functionsVars) {
      if ((index = variableIndex(functionsVar, headings)) > -1) {
        if (index > row.size() - 1) {
          return AppConstants.EMPTY;
        }
        str = StringUtils.replaceOnce(str, functionsVar, row.get(index));
      } else if (FunctionUtil.isFunction(functionsVar, false)) {
        String functionType = FunctionUtil.getFunctionType(functionsVar);
        if (functionType != null) {
          ICallistoFunction function = functionManager.getFunction(functionType);
          if (function == null) {
            throw new CallistoException("Q001", functionsVar);
          }
          FunctionParam functionParam =
              new FunctionParam(request, headings, row, functionsVar, resultSet);
          functionParam.setDimensions(request.dimensions);
          str = StringUtils.replaceOnce(str, functionsVar, function.getResult(functionParam));
        } else {
          throw new CallistoException("Q001", functionsVar);
        }
      }
    }
    return str;
  }

  /**
   * @param val derived value to be parsed
   * @param headings original result headings
   * @return checks if val is only a variable i.e. $xyz, and return index of variable in heading,
   *     otherwise -1
   */
  public static int variableIndex(String val, List<String> headings) {
    if (headings != null
        && val.startsWith(String.valueOf(AppConstants.DOLLAR))
        && !val.contains(AppConstants.FN_ENCLOSE)) {
      for (int i = 0; i < headings.size(); i++) {
        String column = headings.get(i);
        if (column.equalsIgnoreCase(val.substring(1))) {
          return i;
        }
      }
      logger.error(
          "Variable " + val + " not found in heading of QueryResult " + headings.toString());
    }
    return -1;
  }

  /**
   * function to parse DerivedColumn map from an expression and add the existing columns in the same
   * order
   *
   * @param results original QueryResults
   * @return parsed Map
   */
  public LinkedHashMap<String, String> getDerivedColumnsMap(
      String expression, QueryResults results) {
    if (CollectionUtils.isEmpty(results.getHeadings())) {
      return new LinkedHashMap<>(0);
    }
    LinkedHashMap<String, String> derivedResults =
        getRawColumnsAsDerivedColumns(results.getHeadings());
    Type type = new TypeToken<LinkedHashMap<String, String>>() {}.getType();
    // putAll on LinkedHashMap will not change the insertion order
    derivedResults.putAll(new Gson().fromJson(expression, type));
    return derivedResults;
  }

  private LinkedHashMap<String, String> getRawColumnsAsDerivedColumns(List<String> headings) {
    return headings.stream()
        .map(s -> Pair.of(s, AppConstants.DOLLAR + s))
        .collect(
            Collectors.toMap(
                Pair::getFirst, Pair::getSecond, linkedHashMapMerger, LinkedHashMap::new));
  }

  /**
   * @param existingDerivedResults
   * @param columns
   * @param rawResultHeadings
   * @return
   */
  public Map<String, String> getCompleteDerivedColumnsMap(
      Map<String, String> existingDerivedResults,
      Set<String> columns,
      List<String> rawResultHeadings) {
    Map<String, String> derivedResults = new HashMap<>();
    derivedResults.putAll(existingDerivedResults);
    List<String> resultHeadingsCopy = new ArrayList<>(rawResultHeadings);
    resultHeadingsCopy.removeAll(columns);
    derivedResults.putAll(getRawColumnsAsDerivedColumns(resultHeadingsCopy));
    return derivedResults;
  }

  @Autowired
  public void setFunctionManager(FunctionManager functionManager) {
    this.functionManager = functionManager;
  }
}
