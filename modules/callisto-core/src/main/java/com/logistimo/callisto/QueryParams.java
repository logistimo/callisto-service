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

import com.logistimo.callisto.exception.CallistoException;
import com.logistimo.callisto.function.FunctionType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author Mohan Raja
 */
public class QueryParams {
  private FunctionType type;
  private String queryID;
  private Integer size;
  private Integer offset;
  private boolean fill;

  public FunctionType getType() {
    return type;
  }

  public String getQueryID() {
    return queryID;
  }

  public Integer getSize() {
    return size;
  }

  public Integer getOffset() {
    return offset;
  }

  public boolean isFill() {
    return fill;
  }

  private static final Logger logger = LoggerFactory.getLogger(QueryParams.class);

  public static QueryParams getQueryParams(String functionText, Map<String, String> filters)
      throws CallistoException {
    QueryParams queryParams = new QueryParams();
    int index = functionText.indexOf(AppConstants.OPEN_BRACKET);
    if (index == -1) {
      logger.warn("Invalid queryParams found. " + queryParams);
      throw new CallistoException("Q001", functionText);
    }
    String[] functionParams;
    queryParams.type =
        FunctionType.getFunctionType(
            functionText.substring(AppConstants.FN_ENCLOSE.length(), index).trim());
    functionParams =
        functionText
            .substring(index + 1, functionText.indexOf(AppConstants.CLOSE_BRACKET))
            .trim()
            .split(AppConstants.COMMA);
    if (functionParams.length >= 1) {
      String qId = functionParams[0].trim();
      if (filters.containsKey(qId)) {
        queryParams.queryID = filters.get(qId);
      } else {
        queryParams.queryID = qId;
      }
    }
    if (functionParams.length >= 2) {
      String size = functionParams[1].trim();
      if (filters.containsKey(size)) {
        queryParams.size = Integer.parseInt(filters.get(size));
      } else {
        queryParams.size = Integer.parseInt(size);
      }
    }
    if (functionParams.length >= 3) {
      String offset = functionParams[2].trim();
      if (filters.containsKey(offset)) {
        queryParams.offset = Integer.parseInt(filters.get(offset));
      } else {
        queryParams.offset = Integer.parseInt(offset);
      }
    }
    if (functionParams.length >= 4) {
      queryParams.fill = Boolean.parseBoolean(functionParams[3].trim());
    }
    return queryParams;
  }

}
