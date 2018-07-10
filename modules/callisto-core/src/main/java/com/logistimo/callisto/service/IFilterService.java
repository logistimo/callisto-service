package com.logistimo.callisto.service;

import com.logistimo.callisto.QueryResults;
import com.logistimo.callisto.model.Filter;

import java.util.List;

public interface IFilterService {
  QueryResults getFilterResults(String userId, String id, String search);
  List<Filter> getFiltersForUserId(String userId);
}