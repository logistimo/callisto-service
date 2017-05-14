package com.logistimo.callisto.service;

import com.logistimo.callisto.DataSourceType;
import com.logistimo.callisto.QueryResults;
import com.logistimo.callisto.model.ServerConfig;

import java.util.Map;
import java.util.Optional;

/**
 * @author Mohan Raja
 */
public interface IDataBaseService {
  QueryResults fetchRows(ServerConfig config, String query, Map<String, String> filters,
                         Optional<Integer> size, Optional<Integer> offset);

  DataSourceType getMetaFields();
}
