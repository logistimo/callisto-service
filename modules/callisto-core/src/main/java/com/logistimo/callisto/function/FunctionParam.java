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

package com.logistimo.callisto.function;

import static org.apache.commons.collections4.SetUtils.emptyIfNull;

import com.logistimo.callisto.QueryResults;
import com.logistimo.callisto.model.QueryRequestModel;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Wrapper class for holding different parameters of Functions Created by chandrakant on 25/05/17.
 */
public class FunctionParam {

  private QueryRequestModel request;
  private List<String> resultHeadings;
  private List<String> resultRow;
  private List<String> rowHeadings; // row headings of QueryResult
  public String function;
  private String escaping;
  // Complete result set, don't operate on this object directly. For read-only purpose
  private QueryResults resultSet;
  // dimension set
  private Set<String> dimensions;

  public FunctionParam() {
  }

  public FunctionParam(QueryRequestModel request, String escaping, List<String> rowHeadings) {
    this.request = request;
    this.escaping = escaping;
    this.rowHeadings = rowHeadings;
  }

  public FunctionParam(
      QueryRequestModel request,
      List<String> headings,
      List<String> row,
      String function,
      QueryResults results) {
    this.request = request;
    this.resultHeadings = headings;
    this.resultRow = row;
    this.function = function;
    this.resultSet = results;
  }

  public QueryRequestModel getRequest() {
    return request;
  }

  public void setRequest(QueryRequestModel request) {
    this.request = request;
  }

  public List<String> getResultRow() {
    return resultRow;
  }

  public void setResultRow(List<String> resultRow) {
    this.resultRow = resultRow;
  }

  public List<String> getResultHeadings() {
    return resultHeadings;
  }

  public void setResultHeadings(List<String> resultHeadings) {
    this.resultHeadings = resultHeadings;
  }

  public QueryRequestModel getQueryRequestModel() {
    return request;
  }

  public void setQueryRequestModel(QueryRequestModel request) {
    this.request = request;
  }

  String getEscaping() {
    return escaping;
  }

  public void setEscaping(String escaping) {
    this.escaping = escaping;
  }

  List<String> getRowHeadings() {
    return rowHeadings;
  }

  public void setRowHeadings(List<String> rowHeadings) {
    this.rowHeadings = rowHeadings;
  }

  List<String> getRowsCopySortedByColumn(String column, Set<String> columnsToAggregate,
      Set<String> dimensionKeys) {
    List<String> rowCopy = new ArrayList<>(resultRow);
    if (this.resultSet != null
        && CollectionUtils.isNotEmpty(this.resultSet.getRows())
        && CollectionUtils.isNotEmpty(resultHeadings)) {
      List<List<String>> copyOfRows = new ArrayList<>(resultSet.getRows());
      int sortByColumnIndex = getColumnIndex(column);
      if (sortByColumnIndex >= 0) {
        final int finalColumnIndex = sortByColumnIndex;
        copyOfRows.sort(
            (row1, row2) ->
                StringUtils.compare(row1.get(finalColumnIndex), row2.get(finalColumnIndex)));
        Set<String> columns = new HashSet<>(columnsToAggregate);
        columns.addAll(dimensionKeys);
        Map<String, Integer> columnsIndices =
            columns.stream().collect(Collectors.toMap(c -> c, this::getColumnIndex));
        Map<String, BigDecimal> aggregatedColumnValues =
            columnsToAggregate.stream().collect(Collectors.toMap(c -> c, c -> BigDecimal.ZERO));
        for (List<String> row : copyOfRows) {
          if (StringUtils.compare(row.get(finalColumnIndex), resultRow.get(finalColumnIndex)) > 0) {
            break;
          }
          if (dimensionKeys.stream().anyMatch(dimensionKey ->
              StringUtils.isEmpty(resultRow.get(columnsIndices.get(dimensionKey)))
                  || StringUtils.isEmpty(row.get(columnsIndices.get(dimensionKey)))
                  || !Objects.equals(resultRow.get(columnsIndices.get(dimensionKey)),
                  row.get(columnsIndices.get(dimensionKey))))) {
            continue;
          }
          aggregatedColumnValues
              .entrySet()
              .forEach(
                  entry -> {
                    String v = row.get(columnsIndices.get(entry.getKey()));
                    BigDecimal value =
                        StringUtils.isNotEmpty(v) ? new BigDecimal(v) : BigDecimal.ZERO;
                    entry.setValue(entry.getValue().add(value));
                  });
        }
        aggregatedColumnValues.forEach(
            (key, value) -> rowCopy.set(columnsIndices.get(key), String.valueOf(value)));
      }
    }
    return rowCopy;
  }

  Optional<String> getPreviousNonZeroValueOfColumnIfPresent(String column, String sortByColumn,
      String sortByColumnValue, Set<String> dimensionKeys) {
    if (this.resultSet != null
        && CollectionUtils.isNotEmpty(this.resultSet.getRows())
        && CollectionUtils.isNotEmpty(resultHeadings)) {
      List<List<String>> copyOfRows = new ArrayList<>(resultSet.getRows());

      int columnIndex = getColumnIndex(column);
      int sortByColumnIndex = getColumnIndex(sortByColumn);
      Map<String, Integer> dimensionIndices = emptyIfNull(dimensionKeys).stream()
          .collect(Collectors.toMap(
              Function.identity(), this::getColumnIndex));
      List<String> maxSortedColumnValueRow = null;
      if (columnIndex >= 0 && sortByColumnIndex >= 0) {
        for (List<String> row : copyOfRows) {
          if ((maxSortedColumnValueRow == null ||
              StringUtils.compare(row.get(sortByColumnIndex),
                  maxSortedColumnValueRow.get(sortByColumnIndex)) > 0)
              && StringUtils.compare(row.get(sortByColumnIndex), sortByColumnValue) < 0
              && StringUtils.isNoneEmpty(row.get(columnIndex))
              && BigDecimal.ZERO.compareTo(new BigDecimal(row.get(columnIndex))) < 0
              && dimensionIndices.entrySet().stream()
              .allMatch(e -> Objects.equals(row.get(e.getValue()), resultRow.get(e.getValue())))) {
            maxSortedColumnValueRow = row;
          }
        }
      }
      if (maxSortedColumnValueRow != null) {
        return Optional.of(maxSortedColumnValueRow.get(columnIndex));
      }
    }
    return Optional.empty();
  }

  private int getColumnIndex(String column) {
    int columnIndex = -1;
    for (int i = 0; i < resultHeadings.size(); i++) {
      if (Objects.equals(resultHeadings.get(i), column)) {
        columnIndex = i;
      }
    }
    return columnIndex;
  }

  public QueryResults getResultSet() {
    return resultSet;
  }

  public Set<String> getDimensions() {
    return dimensions;
  }

  public void setDimensions(Set<String> dimensions) {
    this.dimensions = dimensions;
  }
}
