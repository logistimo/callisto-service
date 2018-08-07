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

package com.logistimo.callisto.reports;

import com.google.gson.JsonArray;

import com.logistimo.callisto.ICallistoFunction;
import com.logistimo.callisto.QueryResults;
import com.logistimo.callisto.function.FunctionParam;
import com.logistimo.callisto.model.Filter;
import com.logistimo.callisto.reports.core.ReportDataJsonFormatter;
import com.logistimo.callisto.service.IFilterService;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static org.mockito.Matchers.argThat;

@RunWith(SpringJUnit4ClassRunner.class)
public class ReportDataJsonFormatterTest {

  private ReportDataJsonFormatter reportDataJsonFormatter;
  private ICallistoFunction linkFunction;
  private IFilterService filterService;

  @Before
  public void setUp() {
    reportDataJsonFormatter = new ReportDataJsonFormatter();
    linkFunction = Mockito.mock(ICallistoFunction.class);
    reportDataJsonFormatter.setLinkFunction(linkFunction);

    filterService = Mockito.mock(IFilterService.class);
    reportDataJsonFormatter.setFilterService(filterService);
  }

  @Test
  public void testFormatReportData() {
    
    Set<String> metricKeys = new HashSet<>(Arrays.asList("cool-metric", "boring-metric",
        "foo-metric", "bar-metric"));
    QueryResults queryResults = new QueryResults();
    queryResults.setHeadings(Arrays.asList("cool-metric", "did", "bar-metric",
        "kid", "foo-metric", "boring-metric"));
    queryResults.addRow(Arrays.asList("10", "192837", "15", "918273", "18", "22"));
    queryResults.addRow(Arrays.asList("100", "192837", "135", "827364", "155", "140"));

    Mockito.when(filterService.getFilter("logistimo", "did"))
        .thenReturn
            (Optional.of(getFilterWithRenameQueryId("cool-rename-query-id", "TOKEN_COOL_DIMEN")));
    Mockito.when(linkFunction.getResult(argThat(new FunctionParamArgumentMatcher(
        "$$link(cool-rename-query-id)$$", "TOKEN_COOL_DIMEN", "192837"))))
        .thenReturn("Cool dimension display name");


    Mockito.when(filterService.getFilter("logistimo", "kid"))
        .thenReturn(Optional.of(
                getFilterWithRenameQueryId("boring-rename-query-id", "TOKEN_BORING_DIMEN")));

    Mockito.when(linkFunction.getResult(argThat(new FunctionParamArgumentMatcher(
        "$$link(boring-rename-query-id)$$", "TOKEN_BORING_DIMEN", "918273"))))
        .thenReturn("Boring dimension display name");
    Mockito.when(linkFunction.getResult(argThat(new FunctionParamArgumentMatcher(
        "$$link(boring-rename-query-id)$$", "TOKEN_BORING_DIMEN", "827364"))))
        .thenReturn("Uber boring dimension display name");

    JsonArray results = (JsonArray) reportDataJsonFormatter.getFormattedResult("logistimo", metricKeys,
        queryResults);

    Assert.assertNotEquals(results, null);

    Assert.assertEquals(2, results.size());

    Assert.assertTrue(!results.get(0).getAsJsonObject().has("cool-metric")
                      && !results.get(1).getAsJsonObject().has("cool-metric"));
    Assert.assertTrue(!results.get(0).getAsJsonObject().has("bar-metric")
                      && !results.get(1).getAsJsonObject().has("bar-metric"));
    Assert.assertTrue(!results.get(0).getAsJsonObject().has("foo-metric")
                      && !results.get(1).getAsJsonObject().has("foo-metric"));
    Assert.assertTrue(!results.get(0).getAsJsonObject().has("boring-metric")
                      && !results.get(1).getAsJsonObject().has("boring-metric"));

    Assert.assertTrue(results.get(0).getAsJsonObject().has("did")
                      && results.get(1).getAsJsonObject().has("did"));
    Assert.assertTrue(results.get(0).getAsJsonObject().has("kid")
                      && results.get(1).getAsJsonObject().has("kid"));

    Assert.assertTrue(results.get(0).getAsJsonObject().get("metrics").getAsJsonObject().has("cool-metric")
                      && results.get(1).getAsJsonObject().get("metrics").getAsJsonObject().has("cool-metric"));
    Assert.assertTrue(results.get(0).getAsJsonObject().get("metrics").getAsJsonObject().has("foo-metric")
                      && results.get(1).getAsJsonObject().get("metrics").getAsJsonObject().has("foo-metric"));

    Assert.assertTrue(Objects.equals(results.get(0).getAsJsonObject().get("metrics")
        .getAsJsonObject().get("cool-metric").getAsString(), "10")
                      && Objects.equals(results.get(1).getAsJsonObject().get("metrics")
        .getAsJsonObject().get("cool-metric").getAsString(), "100"));
  }

  private Filter getFilterWithRenameQueryId(String renameQueryId, String placeholder) {
    Filter filter = new Filter();
    filter.setRenameQueryid(renameQueryId);
    filter.setPlaceholder(placeholder);
    return filter;
  }

  class FunctionParamArgumentMatcher extends ArgumentMatcher<FunctionParam> {

    private String function;
    private String filterKey;
    private String filterValue;
    FunctionParamArgumentMatcher(String function, String filterKey, String filterValue) {
      this.function = function;
      this.filterKey = filterKey;
      this.filterValue = filterValue;
    }

    @Override
    public boolean matches(Object o) {
      if(o == null) {
        return false;
      }
      FunctionParam functionParam = (FunctionParam) o;
      return Objects.equals(function, functionParam.function)
             && Objects.equals(functionParam.getQueryRequestModel().filters.get(filterKey),
          filterValue);
    }
  }

}