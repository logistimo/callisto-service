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

package com.logistimo.callisto.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.logistimo.callisto.QueryResults;
import com.logistimo.callisto.exception.CallistoException;
import com.logistimo.callisto.ResultManager;
import com.logistimo.callisto.model.ConstantText;
import com.logistimo.callisto.model.QueryRequestModel;
import com.logistimo.callisto.model.QueryText;
import com.logistimo.callisto.service.IConstantService;
import com.logistimo.callisto.service.IQueryService;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author Mohan Raja
 * @author Chandrakant
 */
@RestController
@RequestMapping("/query")
public class QueryController {

  @Resource IQueryService queryService;
  @Resource IConstantService constantService;

  @Autowired ResultManager resultManager;

  @RequestMapping(value = "/save", method = RequestMethod.PUT)
  public String saveQuery(@RequestBody QueryText queryText) {
    return queryService.saveQuery(queryText);
  }

  @RequestMapping(value = "/udpate", method = RequestMethod.POST)
  public String updateQuery(@RequestBody QueryText queryText) {
    return queryService.updateQuery(queryText);
  }

  @RequestMapping(value = "/get", method = RequestMethod.GET)
  public QueryText getQuery(
      @RequestParam(defaultValue = "logistimo") String userId, @RequestParam String queryId) {
    return queryService.readQuery(userId, queryId);
  }

  @RequestMapping(value = "/getdata", method = RequestMethod.POST)
  public String getQueryData(@RequestBody QueryRequestModel model) throws CallistoException {
    QueryResults q =
        queryService.readData(model);
    if (StringUtils.isNotEmpty(model.desiredResultId)) {
      ConstantText constant = constantService.readConstant(model.userId, model.desiredResultId);
      if (constant != null) {
        Type type = new TypeToken<LinkedHashMap<String, String>>() {}.getType();
        LinkedHashMap<String, String> myMap = new Gson().fromJson(constant.getConstant(), type);
        q = resultManager.getDesiredResult(model, q, myMap);
      }
    }
    if (q != null) {
      q.setDataTypes(null);
    }
    return new Gson().toJson(q);
  }

  @RequestMapping(value = "/getids", method = RequestMethod.GET)
  public String getAllQueryIds(@RequestParam(defaultValue = "logistimo") String userId) {
    List<String> queryIds = queryService.readQueryIds(userId);
    return new Gson().toJson(queryIds);
  }

  @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
  public String deleteQuery(
      @RequestParam(defaultValue = "logistimo") String userId, @RequestParam String queryId) {
    return queryService.deleteQuery(userId, queryId);
  }
}
