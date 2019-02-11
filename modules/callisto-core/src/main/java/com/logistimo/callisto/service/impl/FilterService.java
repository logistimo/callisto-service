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

package com.logistimo.callisto.service.impl;

import com.logistimo.callisto.QueryResults;
import com.logistimo.callisto.model.Filter;
import com.logistimo.callisto.model.QueryRequestModel;
import com.logistimo.callisto.repository.FilterRepository;
import com.logistimo.callisto.service.IFilterService;
import com.logistimo.callisto.service.IQueryService;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class FilterService implements IFilterService {

  private FilterRepository filterRepository;
  private IQueryService queryService;

  @Autowired
  public void setFilterRepository(FilterRepository filterRepository) {
    this.filterRepository = filterRepository;
  }

  @Autowired
  public void setQueryService(IQueryService queryService) {
    this.queryService = queryService;
  }

  @Override
  public Optional<Filter> getFilter(String userId, String filterId) {
    return filterRepository.findOne(userId, filterId);
  }

  @Override
  public QueryResults getFilterAutocompleteResults(String userId, String filterId, String search) {
    Optional<Filter> filter = filterRepository.findOne(userId, filterId);
    QueryRequestModel queryRequestModel = null;
    if(filter.isPresent()) {
      queryRequestModel = constructAutoCompleteQueryRequestModel(filter.get(), search);
    }
    return queryService.readData(queryRequestModel);
  }

  private QueryRequestModel constructAutoCompleteQueryRequestModel(Filter filter, String search) {
    QueryRequestModel queryRequestModel = new QueryRequestModel();
    queryRequestModel.queryId = filter.getDefaultAutoCompleteQueryId();
    Map<String, String> filters = new HashMap<>();
    if(StringUtils.isNotEmpty(filter.getAutoCompletePlaceholder())) {
      filters.put(filter.getAutoCompletePlaceholder(), search);
    }
    queryRequestModel.filters = new HashMap<>(filters);
    return queryRequestModel;
  }

  @Override
  public List<Filter> getFiltersForUserId(String userId) {
    return filterRepository.findByUserId(userId);
  }

}