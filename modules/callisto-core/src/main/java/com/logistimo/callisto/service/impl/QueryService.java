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

package com.logistimo.callisto.service.impl;

import com.logistimo.callisto.DataBaseCollection;
import com.logistimo.callisto.FunctionManager;
import com.logistimo.callisto.ICallistoFunction;
import com.logistimo.callisto.QueryResults;
import com.logistimo.callisto.exception.CallistoException;
import com.logistimo.callisto.function.FunctionParam;
import com.logistimo.callisto.function.FunctionUtil;
import com.logistimo.callisto.model.QueryRequestModel;
import com.logistimo.callisto.model.QueryText;
import com.logistimo.callisto.model.ServerConfig;
import com.logistimo.callisto.repository.QueryRepository;
import com.logistimo.callisto.service.IDataBaseService;
import com.logistimo.callisto.service.IQueryService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Mohan Raja
 * @author Chandrakant
 */
@Service
public class QueryService implements IQueryService {

  private static final Logger logger = LoggerFactory.getLogger(QueryService.class);

  @Autowired
  private QueryRepository queryRepository;

  @Autowired
  private UserService userService;

  @Autowired
  private DataBaseCollection dataBaseCollection;

  @Autowired
  private FunctionManager functionManager;

  public String saveQuery(QueryText q) {
    String res = "failure";
    try {
      List<QueryText> existing = queryRepository.readQuery(q.getUserId(), q.getQueryId());
      if (existing == null || existing.isEmpty()) {
        queryRepository.save(q);
        res = "Query saved successfully";
      } else {
        res = "duplicate query Id, try updating the previous one";
      }
    } catch (Exception e) {
      logger.error("Error while saving query", e);
    }
    return res;
  }

  public String updateQuery(QueryText q) {
    String res = "failure";
    try {
      List<QueryText> queryList = queryRepository.readQuery(q.getUserId(), q.getQueryId());
      if (queryList != null && queryList.size() == 1) {
        q.setId(queryList.get(0).getId());
        queryRepository.save(q);
        res = "Query updated successfully";
      }
    } catch (Exception e) {
      logger.error("Error while updating query", e);
    }
    return res;
  }

  public String deleteQuery(String userId, String queryId) {
    try {
      queryRepository.delete(readQuery(userId, queryId));
    } catch (Exception e) {
      logger.error("Error while deleting query", e);
      return "Error while deleting query";
    }
    return "Query deleted successfully";
  }

  public QueryText readQuery(String userId, String queryId) {
    try {
      List<QueryText> queryList = queryRepository.readQuery(userId, queryId, PageRequest.of(0, 1));
      if (queryList != null && !queryList.isEmpty()) {
        return queryList.get(0);
      }
    } catch (Exception e) {
      logger.error("Error while reading query for userId " + userId + " and queryId " + queryId, e);
    }
    return null;
  }

  public List<String> readQueryIds(String userId) {
    List<String> queryIds = null;
    try {
      List<QueryText> queryList = queryRepository.readQueryIds(userId);
      queryIds = new ArrayList<>(queryList.size());
      for (QueryText queryText : queryList) {
        queryIds.add(queryText.getQueryId());
      }
    } catch (Exception e) {
      logger.error("Error while reading query Ids for userId " + userId, e);
    }
    return queryIds;
  }

  @Override
  public QueryResults readData(QueryRequestModel request)
      throws CallistoException {
    QueryText queryText = readQuery(request.userId, request.queryId);
    List<String> rowHeadings = new ArrayList<>();
    if (queryText == null) {
      logger.warn("Query " + request.queryId + " not found for user " + request.userId);
      return null;
    }
    ServerConfig
        serverConfig =
        userService.readServerConfig(request.userId, queryText.getServerId());
    List<String> functions = FunctionUtil.getAllFunctions(queryText.getQuery());
    if (!functions.isEmpty()) {
      for (String functionText : functions) {
        if (FunctionUtil.isFunction(functionText, false)) {
          ICallistoFunction
              qFunction =
              functionManager.getFunction(FunctionUtil.getFunctionType(functionText));
          FunctionParam
              functionParam =
              new FunctionParam(request, serverConfig.getEscaping(), rowHeadings);
          functionParam.function = functionText;
          String data = qFunction.getResult(functionParam);
          queryText.setQuery(queryText.getQuery().replace(functionText, data));
        }
      }
    }
    QueryResults queryResults =
        executeQuery(serverConfig, queryText.getQuery(), request.filters, request.size,
            request.offset);
    queryResults.setRowHeadings(rowHeadings);
    return queryResults;
  }

  private QueryResults executeQuery(
      ServerConfig serverConfig,
      String query,
      Map<String, String> filters,
      Integer size,
      Integer offset) {
    IDataBaseService dataBaseService =
        dataBaseCollection.getDataBaseService(serverConfig.getType());
    return dataBaseService.fetchRows(
        serverConfig, query, filters, Optional.ofNullable(size), Optional.ofNullable(offset));
  }
}
