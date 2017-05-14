package com.logistimo.callisto.service;

import com.logistimo.callisto.Exception.CallistoException;
import com.logistimo.callisto.QueryResults;
import com.logistimo.callisto.model.QueryText;

import java.util.List;
import java.util.Map;

/** @author Chandrakant */
public interface IQueryService {

  String saveQuery(QueryText q);

  String updateQuery(QueryText q);

  QueryText readQuery(String userId, String queryId);

  List<String> readQueryIds(String userId);

  QueryResults readData(String userId, String queryId, Map<String, String> filters, Integer size, Integer offset)
      throws CallistoException;

  String deleteQuery(String userId, String queryId);
}
