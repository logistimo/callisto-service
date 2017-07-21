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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.logistimo.callisto.CharacterConstants;
import com.logistimo.callisto.ICallistoFunction;
import com.logistimo.callisto.QueryResults;
import com.logistimo.callisto.exception.CallistoException;
import com.logistimo.callisto.model.QueryRequestModel;
import com.logistimo.callisto.service.IQueryService;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;

/**
 * Created by chandrakant on 18/05/17.
 */
@Component(value = "link")
public class LinkFunction implements ICallistoFunction {

  private static final Logger logger = Logger.getLogger(LinkFunction.class);
  private static String name = "link";
  @Resource IQueryService queryService;

  public static List<String> getParameter(String value) {
    String val = value.trim();
    int fnStart = val.indexOf(CharacterConstants.OPEN_BRACKET);
    int fnEnd = val.indexOf(CharacterConstants.CLOSE_BRACKET);
    val = StringUtils.substring(val, fnStart + 1, fnEnd);
    String[] csv = StringUtils.split(val, CharacterConstants.COMMA);
    List<String> params = new ArrayList<>(2);
    params.add(csv[0]);
    StringBuilder linkFilters = new StringBuilder();
    for (int i = 1; i < csv.length; i++) {
      linkFilters.append(csv[i]);
    }
    params.add(linkFilters.toString());
    return params;
  }

  public static String getLink(
      QueryRequestModel request, String val, IQueryService queryService)
      throws CallistoException {
    String queryId = getParameter(val).get(0); // first parameter, queryId
    QueryResults rs = queryService.readData(getNewQueryRequestModel(request, queryId));
    if (rs.getRows().size() == 1 && rs.getRows().get(0).size() == 1) {
      return rs.getRows().get(0).get(0);
    } else {
      logger.warn("Expected result size from Link function "
          + val + " is 1. Actual result: " + rs.toString());
    }
    return null;
  }

  private static QueryRequestModel getNewQueryRequestModel(QueryRequestModel request,
                                                           String queryId) {
    QueryRequestModel newQueryRequestModel = request.copy();
    newQueryRequestModel.queryId = queryId;
    return newQueryRequestModel;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getResult(FunctionParam functionParam) throws CallistoException {
    List<String> params = getParameter(functionParam.function);
    String queryId = params.get(0); // first parameter, queryId
    String linkFilters = params.get(1); //second parameter, filters
    Type type = new TypeToken<HashMap<String, String>>() {
    }.getType();
    Map linkFiltersMap = getLinkFilterMap(functionParam, linkFilters, type);
    return getResult(functionParam, queryId, linkFiltersMap);
  }

  private String getResult(FunctionParam functionParam, String queryId,
                           Map<String, String> linkFiltersMap)
      throws CallistoException {
    functionParam.getQueryRequestModel().filters.putAll(linkFiltersMap);
    QueryResults rs = queryService
        .readData(getNewQueryRequestModel(functionParam.getQueryRequestModel(), queryId));
    if (rs.getRows().size() == 1 && rs.getRows().get(0).size() == 1) {
      return rs.getRows().get(0).get(0);
    } else {
      logger.warn("Expected result size from Link function " + functionParam.function
          + " is 1. Actual result: " + rs.toString());
    }
    return null;
  }

  private Map getLinkFilterMap(FunctionParam functionParam, String linkFilters, Type type)
      throws CallistoException {
    try {
      HashMap<String, String> filterMap = new Gson().fromJson(linkFilters, type);
      return filterMap.entrySet().stream().map(e -> {
        if (StringUtils.contains(e.getValue(),
            CharacterConstants.SINGLE_DOLLAR)) {
          try {
            e.setValue(
                FunctionsUtil.replaceVariables(e.getValue(), functionParam.getResultHeadings(),
                    functionParam.getResultRow()));
          } catch (CallistoException e1) {
            logger.error(
                "Error while getting result for link function: " + functionParam.function,
                e1);
          }
        }
        return e;
      }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    } catch (Exception e) {
      throw new CallistoException(e);
    }
  }

  @Override
  public int getArgsLength() {
    return 2;
  }

  @Override
  public int getMinArgsLength() {
    return -1;
  }

  @Override
  public int getMaxArgLength() {
    return -1;
  }
}
