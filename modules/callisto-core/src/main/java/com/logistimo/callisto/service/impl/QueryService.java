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
import com.logistimo.callisto.ResultManager;
import com.logistimo.callisto.exception.CallistoException;
import com.logistimo.callisto.exception.DuplicateQueryIdException;
import com.logistimo.callisto.function.FunctionParam;
import com.logistimo.callisto.function.FunctionUtil;
import com.logistimo.callisto.model.Datastore;
import com.logistimo.callisto.model.PagedResults;
import com.logistimo.callisto.model.QueryRequestModel;
import com.logistimo.callisto.model.QueryText;
import com.logistimo.callisto.repository.QueryRepository;
import com.logistimo.callisto.service.IDataBaseService;
import com.logistimo.callisto.service.IQueryService;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
  private  DatastoreService datastoreService;

  @Autowired 
  private DataBaseCollection dataBaseCollection;
  
  @Autowired 
  private FunctionManager functionManager;

  public void saveQuery(QueryText q) {
    Optional<QueryText> existing = queryRepository.findOne(q.getUserId(), q.getQueryId());
    if (!existing.isPresent()) {
      queryRepository.save(q);
    } else {
      throw new DuplicateQueryIdException("Query Id already exists");
    }
  }

  public void updateQuery(QueryText q) {
    try {
      Optional<QueryText> queryText = queryRepository.findOne(q.getUserId(), q.getQueryId());
      if (queryText.isPresent()) {
        q.setId(queryText.get().getId());
        queryRepository.save(q);
      }
    } catch (Exception e) {
      throw new CallistoException("Q109", e);
    }
  }

  public void deleteQuery(String userId, String queryId) {
    queryRepository.delete(readQuery(userId, queryId));
  }

  @Override
  public List<String> getAllQueryIds(String userId) {
    List<QueryText> queries = queryRepository.readQueryIds(userId);
    return queries.stream().map(QueryText::getQueryId).collect(Collectors.toList());
  }

  @Override
  public List<QueryText> readQueries(String userId, Pageable pageable) {
    return queryRepository.readQueries(userId, pageable);
  }

  @Override
  public Long getTotalNumberOfQueries(String userId) {
    return queryRepository.getCount(userId);
  }

  @Override
  public PagedResults searchQueriesLike(String userId, String like, Pageable pageable) {
    PagedResults<QueryText> pagedResults = new PagedResults();
    pagedResults.setResult(queryRepository.searchQueriesWithQueryId(userId, like, pageable));
    pagedResults.setTotalSize(queryRepository.getSearchQueriesCount(userId, like));
    return pagedResults;
  }

  public QueryText readQuery(String userId, String queryId) {
    try {
      Page<QueryText> queryList = queryRepository.readQuery(userId, queryId, new PageRequest(0, 1));
      if (queryList != null && queryList.hasContent()) {
        return queryList.iterator().next();
      }
    } catch (Exception e) {
      logger.error("Error while reading query for userId " + userId + " and queryId " + queryId, e);
    }
    return null;
  }

  @Override
  public List<String> readQueryIds(String userId, String like, Pageable pageable) {
    List<String> queryIds = null;
    try {
      List<QueryText> queryList;
      String likeRegex = "";
      if(StringUtils.isNotEmpty(like)){
        likeRegex = like;
      }
      if(StringUtils.isNotEmpty(like) && pageable != null) {
        queryList = queryRepository.readQueryIds(userId, likeRegex, pageable);
      } else if (StringUtils.isNotEmpty(like)) {
        queryList = queryRepository.readQueryIds(userId, likeRegex);
      } else if(pageable != null) {
        queryList = queryRepository.readQueryIds(userId, pageable);
      } else {
        queryList = queryRepository.readQueryIds(userId);
      }
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
    QueryText queryText = getQueryText(request);
    List<String> rowHeadings = new ArrayList<>();
    if (queryText == null) {
      logger.warn("Query " + request.queryId + " not found for user " + request.userId);
      return null;
    }
    Datastore
        datastore =
        datastoreService.get(request.userId, queryText.getDatastoreId());
    List<String> functions = FunctionUtil.getAllFunctions(queryText.getQuery());
    if (!functions.isEmpty()) {
      for (String functionText : functions) {
        if (FunctionUtil.isFunction(functionText, false)) {
          ICallistoFunction
              qFunction =
              functionManager.getFunction(FunctionUtil.getFunctionType(functionText));
          FunctionParam
              functionParam =
              new FunctionParam(request, datastore.getEscaping(), rowHeadings);
          functionParam.function = functionText;
          String data = qFunction.getResult(functionParam);
          queryText.setQuery(queryText.getQuery().replace(functionText, data));
        }
      }
    }
    QueryResults queryResults =
        executeQuery(datastore, queryText.getQuery(), request.filters, request.size,
            request.offset);
    queryResults.setRowHeadings(rowHeadings);
    return queryResults;
  }

  @Override
  public QueryResults readAndModifyData(QueryRequestModel requestModel, ResultManager resultManager) {
      QueryResults results = readData(requestModel);
      if (requestModel.columnText != null && !requestModel.columnText.isEmpty()) {
        //expects only one element
        Map.Entry<String, String> entry = requestModel.columnText.entrySet().iterator().next();
        Map<String, String> parsedColumnData = FunctionUtil
            .parseColumnText(entry.getValue());
        requestModel.filters.put(entry.getKey(), FunctionUtil.extractColumnsCsv(parsedColumnData));
        if (results.getRowHeadings() == null) {
          results.setRowHeadings(requestModel.rowHeadings);
        }
        results = resultManager.getDerivedResults(requestModel, results, parsedColumnData);
      }
      return results;
  }

  private QueryText getQueryText(QueryRequestModel request) {
    QueryText queryText = null;
    if (StringUtils.isNotEmpty(request.queryId)) {
      queryText = readQuery(request.userId, request.queryId);
    } else if (request.query != null) {
      queryText = request.query;
    }
    return queryText;
  }

  private QueryResults executeQuery(
      Datastore datastore,
      String query,
      Map<String, String> filters,
      Integer size,
      Integer offset) {
    IDataBaseService dataBaseService =
        dataBaseCollection.getDataBaseService(datastore.getType());
    return dataBaseService.fetchRows(
        datastore, query, filters, Optional.ofNullable(size), Optional.ofNullable(offset));
  }
}
