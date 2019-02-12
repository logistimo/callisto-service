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

import com.logistimo.callisto.QueryResults;

import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component("list")
public class ReportDataListFormatter extends ReportDataFormatter {

  @Override
  public Object getFormattedResult(String userId, Set<String> metricKeys,
                                   QueryResults queryResults) {
    Set<Integer> nonMetricsIndices = IntStream.range(0, queryResults.getHeadings().size()).boxed()
        .filter(i -> !metricKeys.contains(queryResults.getHeadings().get(i)))
        .filter(i -> isRenameQueryPresent(userId, queryResults.getHeadings().get(i)))
        .collect(Collectors.toCollection(LinkedHashSet::new));

    nonMetricsIndices.forEach(
        i -> queryResults.getHeadings().add(queryResults.getHeadings().get(i) + "_" + "name")
    );

    queryResults.getRows().stream()
        .forEach(results -> nonMetricsIndices.forEach(
            i -> results
                .add(getRenamedValue(userId, queryResults.getHeadings().get(i), results.get(i)))
        ));
    return queryResults;
  }
}