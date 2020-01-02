/*
 * Copyright © 2019 Logistimo.
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

package com.logistimo.callisto.reports.core;

import com.logistimo.callisto.QueryResults;
import com.logistimo.callisto.function.FunctionUtil;
import com.logistimo.callisto.model.CallistoContext;
import com.logistimo.callisto.model.QueryRequestModel;
import com.logistimo.callisto.model.QueryText;
import com.logistimo.callisto.model.ReportConfig;
import com.logistimo.callisto.reports.exception.BadReportRequestException;
import com.logistimo.callisto.reports.model.Periodicity;
import com.logistimo.callisto.reports.model.ReportRequestModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component("2.0.0")
public class DataXReportQueryBuilder implements IReportQueryBuilder {

  private static final String PAGINATE_DIMENSION_PLACEHOLDER_VALUE = "{PLACEHOLDER}";

  @Autowired private CallistoContext context;

  @Override
  public QueryRequestModel getQueryRequestModel(
      ReportRequestModel reportRequestModel, ReportConfig reportConfig) {
    QueryRequestModel queryRequest = new QueryRequestModel();
    queryRequest.userId = reportRequestModel.getUserId();
    queryRequest.filters = new HashMap<>(reportRequestModel.getFilters());
    queryRequest.query = buildQuery(reportConfig.getMetrics(), reportRequestModel);
    Set<String> dimensions = new HashSet<>(reportRequestModel.getFilters().keySet());
    if (StringUtils.isNotEmpty(reportRequestModel.getPaginateBy())) {
      dimensions.add(reportRequestModel.getPaginateBy());
    }
    queryRequest.dimensions = dimensions;
    return queryRequest;
  }

  private QueryText buildQuery(Map<String, String> metrics, ReportRequestModel requestModel) {
    QueryText queryText = new QueryText();
    if (StringUtils.isNotBlank(requestModel.getPaginateBy())
        && !CollectionUtils.isEmpty(requestModel.getPage())) {
      requestModel
          .getFilters()
          .put(requestModel.getPaginateBy(), PAGINATE_DIMENSION_PLACEHOLDER_VALUE);
    } else {
      requestModel.setPaginateBy(null);
      requestModel.setPage(Collections.emptyList());
    }
    requestModel.getFilters().put(DataXUtils.DKEY_DIMENSION_KEY, requestModel.getUserId());

    String dimKeys = getDimKeyBaseValue(requestModel.getFilters());

    String dimensions =
        Optional.ofNullable(requestModel.getPage()).orElse(Collections.emptyList()).stream()
            .map(
                element ->
                    StringUtils.replace(
                        dimKeys, PAGINATE_DIMENSION_PLACEHOLDER_VALUE, element.getValue()))
            .map(element -> "'" + element + "'")
            .reduce((s1, s2) -> s1 + "," + s2)
            .orElse("'" + dimKeys + "'");

    final String query =
        "select "
            + DataXUtils.METRIC_COLUMN
            + ", "
            + DataXUtils.VALUE_COLUMN
            + ", "
            + DataXUtils.DIM_KEY_COLUMN
            + ", "
            + DataXUtils.TIME_COLUMN
            + " from "
            + String.format("%s_aggregations", requestModel.getUserId().toLowerCase())
            + " where "
            + DataXUtils.DIM_KEY_COLUMN
            + " in ("
            + dimensions
            + ") and "
            + DataXUtils.METRIC_COLUMN
            + " in ('"
            + StringUtils.join(FunctionUtil.extractColumnSet(metrics), "','")
            + "') and "
            + getTimeQuerySpecification(
                requestModel.getPeriodicity(), requestModel.getFrom(), requestModel.getTo());
    queryText.setQuery(query);
    queryText.setDatastoreId(context.getDataxDatasourceId());
    return queryText;
  }

  private String getDimKeyBaseValue(Map<String, String> reportRequestFilters) {
    return StringUtils.join(
        DataXUtils.DKEY_DIMENSION_KEY
            + DataXUtils.DIM_KEY_SEPARATOR
            + reportRequestFilters.get(DataXUtils.DKEY_DIMENSION_KEY),
        DataXUtils.DIM_KEY_SEPARATOR,
        DataXUtils.DOMAIN_DIMENSION_KEY
            + DataXUtils.DIM_KEY_SEPARATOR
            + reportRequestFilters.get(DataXUtils.DOMAIN_DIMENSION_KEY),
        reportRequestFilters.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .filter(
                entry ->
                    !entry.getKey().equals(DataXUtils.DOMAIN_DIMENSION_KEY)
                        && !entry.getKey().equals(DataXUtils.DKEY_DIMENSION_KEY))
            .map(entry -> entry.getKey() + DataXUtils.DIM_KEY_SEPARATOR + entry.getValue())
            .reduce((s1, s2) -> s1 + DataXUtils.DIM_KEY_SEPARATOR + s2)
            .map(d -> DataXUtils.DIM_KEY_SEPARATOR + d)
            .orElse(""));
  }

  private String getTimeQuerySpecification(Periodicity periodicity, String from, String to) {
    final String timeQuery =
        DataXUtils.PERIODICITY_COLUMN + " = '%s' and t >= '" + from + "' and t <= '" + to + "'";
    switch (periodicity) {
      case daily:
        return String.format(timeQuery, "d");
      case weekly:
        return String.format(timeQuery, "w");
      case monthly:
        return String.format(timeQuery, "m");
      case yearly:
        return String.format(timeQuery, "y");
      case quarterly:
        return String.format(timeQuery, "q");
      case financial_yearly:
        return String.format(timeQuery, "fy");
      case financial_quarterly:
        return String.format(timeQuery, "fq");
      default:
        throw new BadReportRequestException("Unknown periodicity type: " + periodicity);
    }
  }

  @Override
  public QueryResults postProcessQueryResults(
      QueryResults results, ReportRequestModel reportRequestModel) {
    Map<String, Integer> columnIndices = new HashMap<>();
    for (int i = 0; i < results.getHeadings().size(); i++) {
      columnIndices.put(results.getHeadings().get(i), i);
    }

    List<String> flattenedColumnHeadings =
        new ArrayList<>(reportRequestModel.getFilters().keySet());
    flattenedColumnHeadings.add("t");
    flattenedColumnHeadings.addAll(
        FunctionUtil.extractColumnSet(reportRequestModel.getReportConfig().getMetrics()));

    Map<String, Integer> flattenedColumnIndices = new HashMap<>();
    for (int i = 0; i < flattenedColumnHeadings.size(); i++) {
      flattenedColumnIndices.put(flattenedColumnHeadings.get(i), i);
    }

    QueryResults flattenedQueryResults =
        flattenDataxQueryResults(
            reportRequestModel,
            results,
            columnIndices,
            flattenedColumnIndices,
            flattenedColumnHeadings.size());
    flattenedQueryResults.setHeadings(flattenedColumnHeadings);
    return flattenedQueryResults;
  }

  private QueryResults flattenDataxQueryResults(
      ReportRequestModel reportRequestModel,
      QueryResults results,
      Map<String, Integer> columnIndices,
      Map<String, Integer> flattenedColumnIndices,
      int numberOfColumns) {
    QueryResults flattenedResults = new QueryResults();
    results.getRows().stream()
        .collect(
            Collectors.groupingBy(row -> row.get(columnIndices.get(DataXUtils.DIM_KEY_COLUMN))))
        .entrySet()
        .stream()
        .filter(e -> StringUtils.isNotEmpty(e.getKey()))
        .forEach(
            (entry) -> {
              String dimKey = entry.getKey();
              List<List<String>> dimKeyRows = entry.getValue();
              dimKeyRows.stream()
                  .collect(
                      Collectors.groupingBy(
                          row -> row.get(columnIndices.get(DataXUtils.TIME_COLUMN))))
                  .forEach(
                      (time, dimKeyTimeRows) -> {
                        List<String> row =
                            new ArrayList<>(Collections.nCopies(numberOfColumns, ""));
                        String[] dimensionKeyValuePairs =
                            StringUtils.split(dimKey, DataXUtils.DIM_KEY_SEPARATOR);
                        for (int i = 0; i < dimensionKeyValuePairs.length; i += 2) {

                          if (flattenedColumnIndices.containsKey(dimensionKeyValuePairs[i])) {
                            row.set(
                                flattenedColumnIndices.get(dimensionKeyValuePairs[i]),
                                dimensionKeyValuePairs[i + 1]);
                          }
                        }
                        row.set(flattenedColumnIndices.get(DataXUtils.TIME_COLUMN), time);
                        dimKeyTimeRows.forEach(
                            r ->
                                row.set(
                                    flattenedColumnIndices.get(
                                        r.get(columnIndices.get(DataXUtils.METRIC_COLUMN))),
                                    r.get(columnIndices.get(DataXUtils.VALUE_COLUMN))));
                        flattenedResults.addRow(row);
                      });
            });
    return flattenedResults;
  }
}
