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
import com.logistimo.callisto.exception.CallistoException;

import org.apache.log4j.Logger;

import java.util.Map;

/**
 * @author Mohan Raja
 */
public class QueryFunction {
  public FunctionType type;
  public String queryID;
  public Integer size;
  public Integer offset;
  public boolean fill;

  private static final Logger logger = Logger.getLogger(QueryFunction.class);

  public static QueryFunction getQueryFunction(String functionText, Map<String, String> filters)
      throws CallistoException {
    QueryFunction function = new QueryFunction();
    int index = functionText.indexOf(CharacterConstants.OPEN_BRACKET);
    if (index == -1) {
      logger.warn("Invalid function found. " + function);
      throw new CallistoException("Q001", functionText);
    }
    String[] functionParams;
    function.type =
        FunctionType.getFunctionType(
            functionText.substring(CharacterConstants.FN_ENCLOSE.length(), index).trim());
    functionParams =
        functionText
            .substring(index + 1, functionText.indexOf(CharacterConstants.CLOSE_BRACKET))
            .trim()
            .split(CharacterConstants.COMMA);
    if (functionParams.length >= 1) {
      String qId = functionParams[0].trim();
      if (filters.containsKey(qId)) {
        function.queryID = filters.get(qId);
      } else {
        function.queryID = qId;
      }
    }
    if (functionParams.length >= 2) {
      String size = functionParams[1].trim();
      if (filters.containsKey(size)) {
        function.size = Integer.parseInt(filters.get(size));
      } else {
        function.size = Integer.parseInt(size);
      }
    }
    if (functionParams.length >= 3) {
      String offset = functionParams[2].trim();
      if (filters.containsKey(offset)) {
        function.offset = Integer.parseInt(filters.get(offset));
      } else {
        function.offset = Integer.parseInt(offset);
      }
    }
    if (functionParams.length >= 4) {
      function.fill = Boolean.parseBoolean(functionParams[3].trim());
    }
    return function;
  }

}
