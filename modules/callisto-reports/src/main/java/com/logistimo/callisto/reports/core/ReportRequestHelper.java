/*
 * Copyright © 2018 Logistimo.
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

package com.logistimo.callisto.reports.core;

import com.logistimo.callisto.function.FunctionUtil;
import com.logistimo.callisto.model.Filter;
import com.logistimo.callisto.model.QueryRequestModel;
import com.logistimo.callisto.model.ReportConfig;
import com.logistimo.callisto.reports.ReportRequestModel;
import com.logistimo.callisto.service.IFilterService;
import com.logistimo.callisto.service.IQueryService;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.NonNull;

@Component
public class ReportRequestHelper {

  private final static String QUERY_ID_DELIMITER = "_";

  private ReportConfig reportConfig;
  private IFilterService filterService;
  private IQueryService queryService;

  @Autowired
  public void setFilterService(IFilterService filterService) {
    this.filterService = filterService;
  }

  @Autowired
  public void setQueryService(IQueryService queryService) {
    this.queryService = queryService;
  }

  public QueryRequestModel getQueryRequestModel(ReportRequestModel reportRequestModel,
                                                @NonNull ReportConfig reportConfig) {
    this.reportConfig = reportConfig;
    QueryRequestModel queryRequestModel = new QueryRequestModel();
    queryRequestModel.userId = reportRequestModel.getUserId();
    queryRequestModel.filters = new HashMap<>(generateQueryFilters(reportRequestModel.getUserId(),
        reportRequestModel.getFilters()));
      queryRequestModel.queryId = deriveQueryIdFromFilters(reportRequestModel.getUserId(),
          reportRequestModel.getFilters().keySet());
    return queryRequestModel;
  }

  private Map<String, String> generateQueryFilters(String userId, Map<String, String> reportFilters) {
    Map<String, String> callistoFilters = new HashMap<>();
    if(reportFilters != null) {
      for(Map.Entry<String, String> entry : reportFilters.entrySet()) {
        Optional<Filter> filter = filterService.getFilter(userId, entry.getKey());
        if(filter.isPresent()) {
          callistoFilters.put(filter.get().getPlaceholder(), entry.getValue());
        }
      }
    }
    Optional<Filter>
        columnFilter = filterService.getFilter(userId, reportConfig.getColumnFilterId());
    if(columnFilter.isPresent()) {
      Set<String> columns = getColumnsFromMetrics(reportConfig.getMetrics());
      callistoFilters.put(columnFilter.get().getPlaceholder(), StringUtils.join(columns, ","));
    }
    return callistoFilters;
  }

  public Set<String> getColumnsFromMetrics(Map<String, String> metrics) {
    return FunctionUtil.extractColumnSet(metrics);
  }

  /**
   * @param userId userId against which the queries are registered
   * @param filterKeys set of filter keys for which the report is requested
   * @return best suitable queryId from the list of all registered queryIds
   */
  public String deriveQueryIdFromFilters(String userId, Set<String> filterKeys) {
    List<String> queryIds = queryService.getAllQueryIds(userId);
    int maxFiltersFound = Integer.MIN_VALUE;
    int minRemainingFiltersInQueryId = Integer.MAX_VALUE;
    String bestQueryId = null;
    for(final String queryId : queryIds) {
      Set<String> dimensions = Arrays.stream(queryId.split(QUERY_ID_DELIMITER))
          .filter(StringUtils::isNotEmpty)
          .map(String::toLowerCase)
          .collect(Collectors.toSet());
      int filtersFound = 0;
      for(String filterId : filterKeys) {
        if(dimensions.contains(filterId.toLowerCase())) {
          filtersFound++;
        }
      }
      int remainingFiltersInQueryId = dimensions.size() - filtersFound;
      if(maxFiltersFound < filtersFound) {
        maxFiltersFound = filtersFound;
        minRemainingFiltersInQueryId = remainingFiltersInQueryId;
        bestQueryId = queryId;
      } else if(maxFiltersFound == filtersFound) {
        if(minRemainingFiltersInQueryId > remainingFiltersInQueryId) {
          minRemainingFiltersInQueryId = remainingFiltersInQueryId;
          bestQueryId = queryId;
        }
      }
    }
    return bestQueryId;
  }
}