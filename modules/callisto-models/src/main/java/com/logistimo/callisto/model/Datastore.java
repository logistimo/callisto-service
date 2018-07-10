/*
 * Copyright © 2017 Logistimo.
 *
 * This file is part of Logistimo.
 *
 * Logistimo software is a mobile & web platform for supply chain management and remote temperature monitoring in
 * low-resource settings, made available under the terms of the GNU Affero General Public License (AGPL).
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 *
 * You can be released from the requirements of the license by purchasing a commercial license. To know more about
 * the commercial license, please contact us at opensource@logistimo.com
 */

package com.logistimo.callisto.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

import java.util.List;

/**
 * @author Chandrakant
 */
public class Datastore {

  @Id
  private String id;

  @JsonProperty("userId")
  private String userId;

  @JsonProperty("name")
  private String name;

  @JsonProperty("hosts")
  private List<String> hosts;

  @JsonProperty("port")
  private Integer port;

  @JsonProperty("username")
  private String username;

  @JsonProperty("password")
  private String password;

  @JsonProperty("schema")
  private String schema;

  @JsonProperty("type")
  private String type;

  @JsonProperty("escaping")
  private String escaping;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<String> getHosts() {
    return hosts;
  }

  public void setHosts(List<String> hosts) {
    this.hosts = hosts;
  }

  public Integer getPort() {
    return port;
  }

  public void setPort(Integer p) {
    this.port = p;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getSchema() {
    return schema;
  }

  public void setSchema(String schema) {
    this.schema = schema;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getEscaping() {
    return escaping;
  }

  public void setEscaping(String escaping) {
    this.escaping = escaping;
  }

  public void copyFrom(Datastore datastore) {
    this.hosts = datastore.getHosts();
    this.escaping = datastore.getEscaping();
    this.name = datastore.getName();
    this.port = datastore.getPort();
    this.username = datastore.getUsername();
    this.password = datastore.getPassword();
    this.schema = datastore.getSchema();
    this.type = datastore.getType();
    this.userId = datastore.getUserId();
  }

  public String getUserId() {
    return userId;
  }

  public Datastore setUserId(String userId) {
    this.userId = userId;
    return this;
  }
}
