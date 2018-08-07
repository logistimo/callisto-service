package com.logistimo.callisto.service.impl;

import com.logistimo.callisto.QueryResults;
import com.logistimo.callisto.model.Filter;
import com.logistimo.callisto.model.QueryRequestModel;
import com.logistimo.callisto.repository.FilterRepository;
import com.logistimo.callisto.service.IDatastoreService;
import com.logistimo.callisto.service.IFilterService;
import com.logistimo.callisto.service.IQueryService;

import org.apache.commons.lang.StringUtils;
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
    queryRequestModel.filters = filters;
    return queryRequestModel;
  }

  @Override
  public List<Filter> getFiltersForUserId(String userId) {
    return filterRepository.findByUserId(userId);
  }

}