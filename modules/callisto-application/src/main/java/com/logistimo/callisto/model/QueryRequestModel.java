package com.logistimo.callisto.model;

import java.util.Map;

/**
 * @author Mohan Raja
 */
public class QueryRequestModel {
  public String userId = "logistimo";
  public String queryId;
  public Map<String, String> filters;
  public Integer size;
  public Integer offset;
}
