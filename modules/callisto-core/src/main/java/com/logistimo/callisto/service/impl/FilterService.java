package com.logistimo.callisto.service.impl;

import com.logistimo.callisto.QueryResults;
import com.logistimo.callisto.model.Filter;
import com.logistimo.callisto.model.QueryRequestModel;
import com.logistimo.callisto.repository.FilterRepository;
import com.logistimo.callisto.service.IDatastoreService;
import com.logistimo.callisto.service.IFilterService;
import com.logistimo.callisto.service.IQueryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FilterService implements IFilterService {

  private FilterRepository filterRepository;
  private IQueryService queryService;
  private IDatastoreService datastoreService;

  @Autowired
  public void setFilterRepository(FilterRepository filterRepository) {
    this.filterRepository = filterRepository;
  }

  @Autowired
  public void setQueryService(IQueryService queryService) {
    this.queryService = queryService;
  }

  @Autowired
  public void setDatastoreService(IDatastoreService datastoreService) {
    this.datastoreService = datastoreService;
  }

  @Override
  public QueryResults getFilterResults(String userId, String filterId, String search) {
    Filter filter = filterRepository.findOne(userId, filterId);
    QueryRequestModel queryRequestModel = constructQueryRequestModel(filter, search);
    return queryService.readData(queryRequestModel);
  }

  private QueryRequestModel constructQueryRequestModel(Filter filter, String search) {
    QueryRequestModel queryRequestModel = new QueryRequestModel();
    queryRequestModel.queryId = filter.getDefaultQueryId();
    Map<String, String> filters = new HashMap<>();
    filters.put(filter.getSearchFilterPlaceholder(), search);
    queryRequestModel.filters = filters;
    return queryRequestModel;
  }

  @Override
  public List<Filter> getFiltersForUserId(String userId) {
    return filterRepository.findByUserId(userId);
  }

}