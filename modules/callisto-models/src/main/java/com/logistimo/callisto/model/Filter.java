package com.logistimo.callisto.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document
public class Filter {
  @Id private String id;

  @JsonProperty("user_id")
  private String userId;

  @Indexed
  @JsonProperty("filter_id")
  private String filterId;

  @JsonProperty("name")
  private String name;

  @JsonProperty("search_filter_placeholder")
  private String searchFilterPlaceholder;

  @JsonProperty("filter_value_column_name")
  private String filterValueColumnName;

  @JsonProperty("filter_display_column_name")
  private String filterDisplayColumnName;

  @JsonProperty("query_ids")
  private List<FilterQueryModel> queryIds;

  @JsonProperty("is_column_filter")
  private boolean isColumnFilter;

  public void setQueryIds(
      List<FilterQueryModel> queryIds) {
    this.queryIds = queryIds;
  }

  public String getDefaultQueryId() {
    if(queryIds != null && !queryIds.isEmpty()) {
      return queryIds.get(0).queryId;
    }
    return null;
  }

  public String getSearchFilterPlaceholder() {
    return searchFilterPlaceholder;
  }

  public void setSearchFilterPlaceholder(String searchFilterPlaceholder) {
    this.searchFilterPlaceholder = searchFilterPlaceholder;
  }

  public class FilterQueryModel {
    @JsonProperty("filter_ids")
    private List<String> filterIds;

    @JsonProperty("query_id")
    private String queryId;

    public List<String> getFilterIds() {
      return filterIds;
    }

    public void setFilterIds(List<String> filterIds) {
      this.filterIds = filterIds;
    }

    public void setQueryId(String queryId) {
      this.queryId = queryId;
    }

    public String getQueryId() {
      return this.queryId;
    }
  }
}