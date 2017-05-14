package com.logistimo.callisto.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.apache.commons.lang.StringUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * @author Chandrakant
 */
@Document
public class User {

  @Id private String id;

  @Indexed(unique = true)
  @JsonProperty("user_id")
  private String userId = "logistimo";

  @JsonProperty("server_configs")
  private List<ServerConfig> serverConfigs;

  public String getId() {
    return this.id;
  }

  public void setId(String _id) {
    this.id = _id;
  }

  public String getUserId() {
    return this.userId;
  }

  public void setUsername(String un) {
    this.userId = un;
  }

  public List<ServerConfig> getServerConfigs() {
    return this.serverConfigs;
  }
}
