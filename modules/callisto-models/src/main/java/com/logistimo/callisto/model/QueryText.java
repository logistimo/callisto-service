package com.logistimo.callisto.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author chandrakant
 * @author Mohan Raja
 */
@Document
public class QueryText {

  @Id private String id;

  @JsonProperty("user_id")
  private String userId = "logistimo";

  @JsonProperty("query_id")
  private String queryId;

  @JsonProperty("query")
  private String query;

  @JsonProperty("server_id")
  private String serverId;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getQueryId() {
    return queryId;
  }

  public void setQueryId(String queryId) {
    this.queryId = queryId;
  }

  public String getQuery() {
    return query;
  }

  public void setQuery(String query) {
    this.query = query;
  }

  public String getServerId() {
    return serverId;
  }

  public void setServerId(String serverId) {
    this.serverId = serverId;
  }
}
