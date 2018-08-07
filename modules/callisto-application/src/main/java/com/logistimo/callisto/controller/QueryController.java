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

import com.logistimo.callisto.QueryResults;
import com.logistimo.callisto.ResultManager;
import com.logistimo.callisto.SuccessResponseDetails;
import com.logistimo.callisto.exception.CallistoException;
import com.logistimo.callisto.function.FunctionUtil;
import com.logistimo.callisto.model.ConstantText;
import com.logistimo.callisto.model.QueryRequestModel;
import com.logistimo.callisto.model.QueryText;
import com.logistimo.callisto.model.ResultsModel;
import com.logistimo.callisto.service.IConstantService;
import com.logistimo.callisto.service.IQueryService;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Mohan Raja
 * @author Chandrakant
 */
@RestController
@RequestMapping("/query")
public class QueryController {

  public static final String RESPONSE_TOTAL_SIZE_HEADER_KEY = "size";

  @Resource IQueryService queryService;
  @Resource IConstantService constantService;

  @Autowired ResultManager resultManager;

  @RequestMapping(value = "", method = RequestMethod.GET)
  public ResponseEntity getQueries(@PageableDefault(page = 0, size = Integer.MAX_VALUE)
                                    Pageable pageable, @RequestParam(defaultValue = "logistimo")
                                    String userId) {
    List<QueryText> queryTexts = queryService.readQueries(userId, pageable);
    Long totalSize = queryService.getTotalNumberOfQueries(userId);
    MultiValueMap<String, String> headers = new HttpHeaders();
    if(totalSize != null) {
      headers.put(RESPONSE_TOTAL_SIZE_HEADER_KEY, Collections.singletonList(String.valueOf
          (totalSize)));
    }
    return new ResponseEntity<>(queryTexts, headers, HttpStatus.OK);
  }

  @RequestMapping(value = "/search/{like}", method = RequestMethod.GET)
  public ResponseEntity getQueriesLike(@PageableDefault(page = 0, size = Integer.MAX_VALUE)
                                    Pageable pageable, @RequestParam(defaultValue = "logistimo")
                                    String userId, @PathVariable String like) {
    ResultsModel resultsModel = queryService.searchQueriesLike(userId, like, pageable);
    MultiValueMap<String, String> headers = new HttpHeaders();
    Long totalSize = resultsModel.totalResultsCount;
    if(totalSize != null) {
      headers.put(RESPONSE_TOTAL_SIZE_HEADER_KEY, Collections.singletonList(String.valueOf
          (totalSize)));
    }
    return new ResponseEntity<>(resultsModel.result, headers, HttpStatus.OK);
  }

  @RequestMapping(value = "/ids", method = RequestMethod.GET)
  public ResponseEntity getQueryIds(@PageableDefault(page = 0, size = Integer.MAX_VALUE)
                                    Pageable pageable, @RequestParam(defaultValue = "logistimo")
                                    String userId) {
    List<String> queryIds = queryService.readQueryIds(userId, null, pageable);
    return new ResponseEntity<>(queryIds, HttpStatus.OK);
  }

  @RequestMapping(value = "/ids/{like}", method = RequestMethod.GET)
  public ResponseEntity getQueryIdsLike(@PathVariable String like, @RequestParam
      (defaultValue = "logistimo") String userId,
                                               @PageableDefault(page = 0, size = Integer.MAX_VALUE)
                                                 Pageable pageable) {
    List<String> queryIds = queryService.readQueryIds(userId, like, pageable);
    return new ResponseEntity<>(queryIds, HttpStatus.OK);
  }

  @RequestMapping(value = "/save", method = RequestMethod.PUT)
  public ResponseEntity saveQuery(@RequestBody QueryText queryText) {
    queryService.saveQuery(queryText);
    SuccessResponseDetails successResponseDetails = new SuccessResponseDetails("Query successfully saved");
    return new ResponseEntity<>(successResponseDetails, HttpStatus.OK);
  }

  @RequestMapping(value = "/udpate", method = RequestMethod.POST)
  public ResponseEntity updateQuery(@RequestBody QueryText queryText) {
    queryService.updateQuery(queryText);
    SuccessResponseDetails responseDetails =
        new SuccessResponseDetails("Query updated successfully");
    return new ResponseEntity<>(responseDetails, HttpStatus.OK);
  }

  @RequestMapping(value = "/{queryId}", method = RequestMethod.GET)
  public ResponseEntity getQuery(
      @RequestParam(defaultValue = "logistimo") String userId, @PathVariable String queryId) {
    QueryText queryText = queryService.readQuery(userId, queryId);
    return new ResponseEntity<>(queryText, HttpStatus.OK);
  }

  @RequestMapping(value = "/getdata", method = RequestMethod.POST)
  public String getQueryData(@RequestBody QueryRequestModel model, HttpServletRequest request)
      throws CallistoException {
    QueryResults results = null;
    if (StringUtils.isNotEmpty(model.derivedResultsId)) {
      results = queryService.readData(model);
      if (results.getRowHeadings() == null) {
        results.setRowHeadings(model.rowHeadings);
      }
      ConstantText constant = constantService.readConstant(model.userId, model.derivedResultsId);
      if (constant != null) {
        Map<String, String> derivedColumns =
            resultManager.getDerivedColumnsMap(constant.getConstant(), results);
        results = resultManager.getDerivedResults(model, results, derivedColumns);
      }
    } else if (Objects.equals(request.getHeader("X-app-version"), "v2")) {
      if (model.columnText != null && !model.columnText.isEmpty()) {
        //expects only one element
        Map.Entry<String, String> entry = model.columnText.entrySet().iterator().next();
        LinkedHashMap<String, String> parsedColumnData = FunctionUtil
            .parseColumnText(entry.getValue());
        model.filters.put(entry.getKey(), FunctionUtil.extractColumnsCsv(parsedColumnData));
        results = queryService.readData(model);
        if (results.getRowHeadings() == null) {
          results.setRowHeadings(model.rowHeadings);
        }
        results = resultManager.getDerivedResults(model, results, parsedColumnData);
      }
    } else {
      results = queryService.readData(model);
    }
    if (results != null) {
      results.setDataTypes(null);
    }
    return new Gson().toJson(results);
  }

  @RequestMapping(value = "/run", method = RequestMethod.POST)
  public ResponseEntity runQuery(@RequestBody QueryRequestModel model, HttpServletRequest request)
      throws CallistoException {
    QueryResults results = null;
    if (Objects.equals(request.getHeader("X-app-version"), "v2")) {
      if (model.columnText != null && !model.columnText.isEmpty()) {
        //expects only one element
        Map.Entry<String, String> entry = model.columnText.entrySet().iterator().next();
        LinkedHashMap<String, String> parsedColumnData = new LinkedHashMap<>();
        if (StringUtils.isNotEmpty(entry.getKey()) && StringUtils.isNotEmpty(entry.getValue())) {
          parsedColumnData = FunctionUtil
              .parseColumnText(entry.getValue());
          if (model.filters == null) {
            model.filters = new HashMap<>();
          }
          model.filters.put(entry.getKey(), FunctionUtil.extractColumnsCsv(parsedColumnData));
        }
        results = queryService.readData(model);
        if (results.getRowHeadings() == null) {
          results.setRowHeadings(model.rowHeadings);
        }
        results = resultManager.getDerivedResults(model, results, parsedColumnData);
      }
    } else if (model.query != null) {
      results = queryService.readData(model);
    }
    return new ResponseEntity<>(results, HttpStatus.OK);
  }

  @RequestMapping(value = "/{queryId}", method = RequestMethod.DELETE)
  public ResponseEntity deleteQuery(
      @RequestParam(defaultValue = "logistimo") String userId, @PathVariable String queryId) {
    queryService.deleteQuery(userId, queryId);
    SuccessResponseDetails successResponseDetails = new SuccessResponseDetails("Query successfully deleted");
    return new ResponseEntity<>(successResponseDetails, HttpStatus.OK);
  }
}
