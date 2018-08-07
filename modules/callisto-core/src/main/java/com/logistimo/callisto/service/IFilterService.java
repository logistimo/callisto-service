package com.logistimo.callisto.service;

import com.logistimo.callisto.QueryResults;
import com.logistimo.callisto.model.Filter;

import java.util.List;
import java.util.Optional;

public interface IFilterService {
  Optional<Filter> getFilter(String userId, String filterId);
  QueryResults getFilterAutocompleteResults(String userId, String id, String search);
  List<Filter> getFiltersForUserId(String userId);
}