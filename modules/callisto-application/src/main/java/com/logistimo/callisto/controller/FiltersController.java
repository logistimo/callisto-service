package com.logistimo.callisto.controller;

import com.logistimo.callisto.QueryResults;
import com.logistimo.callisto.model.Filter;
import com.logistimo.callisto.service.IFilterService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/filter")
public class FiltersController {
  private IFilterService filterService;

  @Autowired
  public void setFilterService(IFilterService filterService) {
    this.filterService = filterService;
  }

  @RequestMapping(value = "", method = RequestMethod.GET)
  public ResponseEntity getFilters(@RequestParam(defaultValue = "logistimo") String userId) {
    List<Filter> filters = filterService.getFiltersForUserId(userId);
    return new ResponseEntity<>(filters, HttpStatus.OK);
  }

  @RequestMapping(value = "/search/{filterId}", method = RequestMethod.GET)
  public ResponseEntity getFilterSearchResults(
      @RequestParam(defaultValue = "logistimo") String userId,
      @PathVariable String filterId,
      @RequestParam String search) {
    QueryResults results = filterService.getFilterAutocompleteResults(userId, filterId, search);
    return new ResponseEntity<>(results, HttpStatus.OK);
  }

}