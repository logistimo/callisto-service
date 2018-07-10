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
import com.logistimo.callisto.ResultManager;
import com.logistimo.callisto.exception.CallistoException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Created by chandrakant on 22/05/17.
 */
public class FunctionUtil {

  private static final Logger logger = Logger.getLogger(FunctionUtil.class);
  private static final String[]
      delimiters =
      {CharacterConstants.ADD, CharacterConstants.COMMA, CharacterConstants.CLOSE_BRACKET,
          CharacterConstants.DIVIDE, CharacterConstants.MULTIPLY, CharacterConstants.SPACE,
          CharacterConstants.PIPE, CharacterConstants.SUBTRACT, CharacterConstants.CLOSE_CBRACKET,
          String.valueOf(CharacterConstants.DOLLAR)};

  private FunctionUtil() {
    // Util class
  }

  public static boolean isFunction(String value, boolean skipEnclose) {
    String val = StringUtils.trim(value);
    if (skipEnclose || FunctionUtil.validateSyntax(val)) {
      int fnStart = StringUtils.indexOf(val, CharacterConstants.OPEN_BRACKET);
      int fnEnd = StringUtils.indexOf(val, CharacterConstants.CLOSE_BRACKET);
      if (fnStart != -1
          && fnEnd != -1
          && fnEnd > fnStart
          && StringUtils.isNotEmpty(StringUtils.substring(val, fnStart + 1, fnEnd))
          && StringUtils.isNotEmpty(
          StringUtils.substring(
              val,
              StringUtils.indexOf(val, CharacterConstants.FN_ENCLOSE)
                  + CharacterConstants.FN_ENCLOSE.length(),
              StringUtils.indexOf(val, CharacterConstants.OPEN_BRACKET)))) {
        return true;
      } else {
        logger.warn("Invalid function: " + val);
      }
    }
    return false;
  }

  public static String getFunctionType(String value) {
    String val = StringUtils.trim(value);
    try {
      return
          StringUtils.substring(
              val, StringUtils.indexOf(val, CharacterConstants.FN_ENCLOSE)
                  + StringUtils.length(CharacterConstants.FN_ENCLOSE),
              StringUtils.indexOf(val, CharacterConstants.OPEN_BRACKET));
    } catch (IllegalArgumentException e) {
      logger.error("Exception while getting function type: " + val, e);
    }
    return null;
  }

  public static boolean validateSyntax(String val) {
    return (StringUtils.startsWith(val, CharacterConstants.FN_ENCLOSE)
        && StringUtils.endsWith(val, CharacterConstants.FN_ENCLOSE));
  }

  public static List<String> getAllFunctions(String text) {
    return getAllFunctions(text, 0);
  }

  public static List<String> getAllFunctions(String text, int start) {
    List<String> matches = new ArrayList<>();
    if (text.contains(CharacterConstants.FN_ENCLOSE)) {
      int ss = text.indexOf(CharacterConstants.FN_ENCLOSE, start);
      int se =
          text.indexOf(CharacterConstants.FN_ENCLOSE, ss + 1)
              + CharacterConstants.FN_ENCLOSE.length();
      String subStr = text.substring(ss, se);
      matches.add(subStr);
      if (text.indexOf(CharacterConstants.FN_ENCLOSE, se + 1) >= 0) {
        matches.addAll(getAllFunctions(text, se + 1));
      }
    }
    return matches;
  }

  public static boolean isDelimiter(char c) {
    String s = String.valueOf(c);
    return Stream.of(delimiters).anyMatch(s::equals);
  }

  private static String getVariable(String text, int startIndex) {
    OptionalInt index =
        Stream.of(delimiters).mapToInt(s -> StringUtils.indexOf(text, s, startIndex + 1))
            .map(i -> i == -1 ? Integer.MAX_VALUE : i).min();
    if (index.isPresent() && index.getAsInt() != Integer.MAX_VALUE) {
      return StringUtils.substring(text, startIndex, index.getAsInt());
    } else {
      return StringUtils.substring(text, startIndex);
    }
  }

  /**
   * @param text String to be parsed
   * @return a list of all the variables and functions in the String. Function uses the fact that
   * variables are prefixed by '$' and functions are enclosed by '$$'.
   */
  public static List<String> getAllFunctionsAndVariables(String text) {
    List<String> matches = new ArrayList<>();
    int sIndex = StringUtils.indexOf(text, CharacterConstants.DOLLAR);
    if (sIndex > -1 && StringUtils.isNotEmpty(text)) {
      int dIndex = text.indexOf(CharacterConstants.FN_ENCLOSE);
      if (dIndex == -1 || dIndex > sIndex) {
        // Variable first
        String var = getVariable(text, sIndex);
        matches.add(var);
        matches
            .addAll(getAllFunctionsAndVariables(StringUtils.substring(text, sIndex + var.length())));
      } else if (dIndex > -1) {
        // sIndex = dIndex i.e. Function first
        int index = StringUtils.indexOf(text, CharacterConstants.FN_ENCLOSE, dIndex + 1);
        if (index > -1) {
          matches.add(
              StringUtils.substring(text, dIndex, index + CharacterConstants.FN_ENCLOSE.length()));
          matches.addAll(getAllFunctionsAndVariables(
              StringUtils.substring(text, index + CharacterConstants.FN_ENCLOSE.length())));
        } else {
          logger.warn("Error in getAllFunctionsVariable: " + text);
        }
      }
    }
    return matches;
  }

  /**
   * @param text      String to be parsed
   * @param indicator Indicator which is a prefix for all the variables
   * @return A list of Variables which are prefixed with an indicator in a String
   */
  public static List<String> getAllVariables(String text, char indicator) {
    List<String> matches = new ArrayList<>();
    try {
      StringBuilder temp = new StringBuilder();
      for (int i = 0; i < text.length(); i++) {
        if (i != text.length() - 1
            && text.charAt(i) == indicator
            && text.charAt(i + 1) != indicator && (i == 0 || text.charAt(i - 1) != indicator)) {
          temp.append(text.charAt(i));
        } else if (FunctionUtil.isDelimiter(text.charAt(i)) && temp.length() > 0) {
          matches.add(temp.toString());
          temp = new StringBuilder();
        } else if (temp.length() > 0) {
          temp.append(text.charAt(i));
        }
      }
      if (temp.length() > 0) {
        matches.add(temp.toString());
      }
    } catch (Exception e) {
      logger.warn("Exception while getting variable list: " + text, e);
    }
    return matches;
  }

  /**
   * Given a string to parse and column list and a result row list, the function replaces the
   * respective column variables from the result row.
   *
   * @param val      String for parsing and modification
   * @param headings List of column names
   * @param row      List of column results, which is a result row in general
   * @return String after all the variables are replaced with respective results
   */
  public static String replaceVariables(String val, List<String> headings, List<String> row)
      throws CallistoException {
    List<String> variables = getAllVariables(val, CharacterConstants.DOLLAR);
    for (int i = 0; i < variables.size(); i++) {
      int index = ResultManager.variableIndex(variables.get(i), headings);
      if (index != -1) {
        if(StringUtils.isNotEmpty(row.get(index))){
          val = StringUtils.replace(val, variables.get(i), row.get(index));
        }else{
          logger.error("Variable "+variables.get(i)+" not found in results. So replacing "
                       + "with empty character in expression: " + val);
          val = StringUtils.replace(val, variables.get(i), CharacterConstants.EMPTY);
        }
      } else {
        if (row.size() == headings.size()) {
          logger.warn("Unknown variable found in Math function: " + val);
          throw new CallistoException("Q102", variables.get(i), headings.toString());
        }
        val = StringUtils.replace(val, variables.get(i), CharacterConstants.EMPTY);
      }
    }
    return val;
  }

  /**
   * parser function to extract key value pair of modified columns from input text
   *
   * @param str String to be parsed
   * @return Map of modified column names and derived values. Values can contain
   * CallistoFunctions and column references as variables from the original result.
   */
  public static LinkedHashMap<String, String> parseColumnText(String str) throws CallistoException {
    try {
      if(StringUtils.isEmpty(str)) {
        return new LinkedHashMap<>();
      }
      String[] splitArr = StringUtils.split(str, CharacterConstants.COMMA);
      for (int i = 0; i < splitArr.length; i++) {
        if (i != splitArr.length - 1
            && StringUtils.countMatches(splitArr[i], CharacterConstants.FN_ENCLOSE) % 2 != 0) {
          splitArr[i + 1] = splitArr[i].concat(CharacterConstants.COMMA).concat(splitArr[i + 1]);
          splitArr[i] = CharacterConstants.EMPTY;
        }
      }
      List<String> columns =
          Arrays.asList(splitArr).stream().filter(StringUtils::isNotEmpty).collect(
              Collectors.toList());
      return columns.stream().collect(Collectors.toMap(s -> {
        String[] split = s.split(CharacterConstants.AS);
        return split.length >= 2 ? split[split.length - 1].trim() : s.trim();
      }, s -> {
        String[] split = s.split(CharacterConstants.AS);
        String var = StringUtils.contains(s, CharacterConstants.DOLLAR) ? s.trim()
            : CharacterConstants.DOLLAR + s.trim();
        return split.length >= 2 ? StringUtils.join(
            IntStream.range(0, split.length).filter(i -> i < split.length - 1)
                .mapToObj(i -> split[i])
                .collect(Collectors.toList()), CharacterConstants.EMPTY).trim() : var;
      }, ResultManager.linkedHashMapMerger, LinkedHashMap::new));
    } catch (Exception e) {
      logger.error("Exception while parsing column text", e);
      throw new CallistoException(e);
    }
  }

  /**
   * @param columnData map of derived column names with the respective values
   * @return a CSV String of all the variables/columns used in the map values.
   */
  public static String extractColumnsCsv(Map<String, String> columnData) {
    Set columns = columnData.entrySet().stream()
        .flatMap(e -> e.getValue().contains(CharacterConstants.FN_ENCLOSE) ? FunctionUtil
            .getAllVariables(e.getValue(), CharacterConstants.DOLLAR).stream()
            .map(s -> s.substring(1))
            : new ArrayList<>(Collections.singletonList(e.getValue().substring(1))).stream())
        .collect(Collectors.toSet());
    return StringUtils.join(columns, CharacterConstants.COMMA);
  }
}
