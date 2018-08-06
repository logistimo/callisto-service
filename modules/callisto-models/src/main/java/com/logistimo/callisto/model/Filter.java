package com.logistimo.callisto.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Set;

@Document
public class Filter {

  @Id
  private String id;

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