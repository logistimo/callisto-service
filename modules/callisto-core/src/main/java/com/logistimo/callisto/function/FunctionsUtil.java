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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by chandrakant on 22/05/17.
 */
public class FunctionsUtil {

  private static final Logger logger = Logger.getLogger(FunctionsUtil.class);

  private FunctionsUtil(){
    // Util class
  }


  public static boolean isFunction(String value, boolean skipEnclose) {
    String val = StringUtils.trim(value);
    if (skipEnclose || FunctionsUtil.functionSyntaxValid(val)) {
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

  public static boolean functionSyntaxValid(String val) {
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
    return Objects.equals(s, CharacterConstants.ADD)
        || Objects.equals(s, CharacterConstants.DIVIDE)
        || Objects.equals(s, CharacterConstants.MULTIPLY)
        || Objects.equals(s, CharacterConstants.SINGLE_DOLLAR)
        || Objects.equals(s, CharacterConstants.SUBTRACT)
        || Objects.equals(s, CharacterConstants.CLOSE_BRACKET);
  }

  private static String getVariable(String text, int startIndex) {
    StringBuilder var = new StringBuilder();
    int index;
    for (index = startIndex; index < text.length(); index++) {
      if (index > startIndex && isDelimiter(text.charAt(index))) {
        break;
      }
      var.append(text.charAt(index));
    }
    return var.toString();
  }

  public static List<String> getAllFunctionsVariables(String text) {
    List<String> matches = new ArrayList<>();
    int sIndex = StringUtils.indexOf(text, CharacterConstants.SINGLE_DOLLAR);
    if (sIndex > -1 && StringUtils.isNotEmpty(text)) {
      int dIndex = text.indexOf(CharacterConstants.FN_ENCLOSE);
      if (dIndex == -1 || dIndex > sIndex) {
        // variable first
        String var = getVariable(text, sIndex);
        matches.add(var);
        matches
            .addAll(getAllFunctionsVariables(StringUtils.substring(text, sIndex + var.length())));
      } else if (dIndex > -1) {
        // sIndex = dIndex i.e. function first
        int index = StringUtils.indexOf(text, CharacterConstants.FN_ENCLOSE, dIndex + 1);
        if (index > -1) {
          matches.add(
              StringUtils.substring(text, dIndex, index + CharacterConstants.FN_ENCLOSE.length()));
          matches.addAll(getAllFunctionsVariables(
              StringUtils.substring(text, index + CharacterConstants.FN_ENCLOSE.length())));
        } else {
          logger.warn("Error in getAllFunctionsVariable: " + text);
        }
      }
    }
    return matches;
  }
}
