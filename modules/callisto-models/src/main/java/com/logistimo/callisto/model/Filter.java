/*
 * Copyright © 2018 Logistimo.
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
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Set;

@Document
public class Filter {

  @Id
  private String id;

  @Indexed
  @JsonProperty("filter_id")
  private String filterId;

  @JsonProperty("user_id")
  private String userId;

  @JsonProperty("placeholder")
  private String placeholder;

  @JsonProperty("name")
  private String name;

  @JsonProperty("auto_complete_config")
  private FilterAutoCompleteConfigModel autoCompleteConfig;

  @JsonProperty("is_column_filter")
  private boolean isColumnFilter;

  @JsonProperty("rename_query_id")
  private String renameQueryId;

  public String getDefaultAutoCompleteQueryId() {
    if (autoCompleteConfig != null) {
      return autoCompleteConfig.getDefaultQueryId();
    }
    return null;
  }

  public String getPlaceholder() {
    return placeholder;
  }

  public String getAutoCompletePlaceholder() {
    if(autoCompleteConfig != null) {
      return autoCompleteConfig.placeholder;
    }
    return null;
  }

  public void setPlaceholder(String placeholder) {
    this.placeholder = placeholder;
  }

  public String getRenameQueryId() {
    return renameQueryId;
  }

  public void setRenameQueryid(String renameQueryId) {
    this.renameQueryId = renameQueryId;
  }

  public class FilterAutoCompleteConfigModel {

    @JsonProperty("placeholder")
    private String placeholder;

    @JsonProperty("value_column_name")
    private String valueColumnName;

    @JsonProperty("display_column_name")
    private String displayColumnName;

    @JsonProperty("auto_complete_queries")
    private List<FilterAutoCompleteQueries> queries;

    public class FilterAutoCompleteQueries {

      @JsonProperty("filter_ids")
      Set<String> filterIds;

      @JsonProperty("query_id")
      String queryId;
    }

    private String getDefaultQueryId() {
      if(queries != null && !queries.isEmpty()) {
        return queries.get(0).queryId;
      }
      return null;
    }
  }
}