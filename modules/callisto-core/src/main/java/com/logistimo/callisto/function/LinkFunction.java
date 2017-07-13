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
import com.logistimo.callisto.QueryResults;
import com.logistimo.callisto.exception.CallistoException;
import com.logistimo.callisto.model.QueryRequestModel;
import com.logistimo.callisto.service.IQueryService;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Created by chandrakant on 18/05/17.
 */
@Component(value = "link")
public class LinkFunction implements ICallistoFunction {

  private static final Logger logger = Logger.getLogger(LinkFunction.class);
  private static String name = "link";
  @Resource IQueryService queryService;

  public static String getParameter(String value) {
    String val = value.trim();
    int fnStart = val.indexOf(CharacterConstants.OPEN_BRACKET);
    int fnEnd = val.indexOf(CharacterConstants.CLOSE_BRACKET);
    return StringUtils.split(
        StringUtils.substring(val, fnStart + 1, fnEnd), CharacterConstants.COMMA)[
        0];
  }

  public static String getLink(
      QueryRequestModel request, String val, IQueryService queryService)
      throws CallistoException {
    String queryId = getParameter(val);
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
    String queryId = getParameter(functionParam.getFunction());
    QueryResults rs =
        queryService.readData(getNewQueryRequestModel(functionParam.getQueryRequestModel(), queryId));
    if (rs.getRows().size() == 1 && rs.getRows().get(0).size() == 1) {
      return rs.getRows().get(0).get(0);
    } else {
      logger.warn("Expected result size from Link function " + functionParam.getFunction()
              + " is 1. Actual result: " + rs.toString());
    }
    return null;
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
}
