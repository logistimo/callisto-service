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

package com.logistimo.callisto.reports.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import com.logistimo.callisto.QueryResults;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component (value = "json")
public class ReportDataJsonFormatter extends ReportDataFormatter {

  private static final String METRICS_KEY = "metrics";
  private static final String DIMENSIONS_KEY = "dimensions";

  private static final String DIMENSION_VALUE_KEY = "value";
  private static final String DIMENSION_NAME_KEY = "name";

  @Override
  public Object getFormattedResult(String userId, Set<String> metricKeys, QueryResults
      queryResults) {
    JsonArray results = new JsonArray();
    for(int i=0;i<queryResults.getRows().size();i++) {
      results.add(getFormattedResult(userId, metricKeys, queryResults.getRows().get(i),
          queryResults.getHeadings()));
    }
    return results;
  }

  private JsonObject getFormattedResult(String userId, Set<String> metricKeys, List<String> row,
                                    List<String> headings) {
    JsonObject result = new JsonObject();
    JsonObject dimensions = new JsonObject();
    JsonObject metrics = new JsonObject();
    for (int i = 0; i < headings.size(); i++) {
      final String heading = headings.get(i);
      if (!metricKeys.contains(heading)) {
        String name = getRenamedValue(userId, heading, row.get(i));
        dimensions.add(heading, transformMetadataToJson(row.get(i), name));
      } else {
        metrics.addProperty(heading, row.get(i));
      }
    }
    result.add(METRICS_KEY, metrics);
    result.add(DIMENSIONS_KEY, dimensions);
    return result;
  }

  private JsonObject transformMetadataToJson(String value, String name) {
    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty(DIMENSION_VALUE_KEY, value);
    jsonObject.addProperty(DIMENSION_NAME_KEY, name);
    return jsonObject;
  }
}