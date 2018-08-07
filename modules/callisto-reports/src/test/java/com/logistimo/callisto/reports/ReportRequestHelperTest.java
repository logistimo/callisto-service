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

import com.logistimo.callisto.model.Filter;
import com.logistimo.callisto.model.QueryRequestModel;
import com.logistimo.callisto.model.ReportConfig;
import com.logistimo.callisto.reports.core.ReportRequestHelper;
import com.logistimo.callisto.service.IFilterService;
import com.logistimo.callisto.service.IQueryService;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
public class ReportRequestHelperTest {

  private IQueryService queryService;
  private IFilterService filterService;
  private ReportRequestHelper reportRequestHelper;

  @Before
  public void setUp() {
    reportRequestHelper = new ReportRequestHelper();
    queryService = Mockito.mock(IQueryService.class);
    reportRequestHelper.setQueryService(queryService);
    filterService = Mockito.mock(IFilterService.class);
    reportRequestHelper.setFilterService(filterService);
    when(queryService.getAllQueryIds("logistimo")).thenReturn(Arrays.asList("DID", "DID_KID",
        "DID_MID", "DID_KID_MID", "DID_KTAG_MID", "DID_KTAG_MID_CN", "DID_KTAG_MID_CN_ST",
        "DID_KTAG_MID_CN_ST_TALUK"));
  }


  @Test
  public void deriveQueryIdFromFilters() {
    String queryId = reportRequestHelper.deriveQueryIdFromFilters("logistimo", new HashSet<>
        (Arrays.asList("did")));
    Assert.assertEquals("DID", queryId);
    queryId = reportRequestHelper.deriveQueryIdFromFilters("logistimo", new HashSet<>
        (Arrays.asList("did","mid")));
    Assert.assertEquals("DID_MID", queryId);

    queryId = reportRequestHelper.deriveQueryIdFromFilters("logistimo", new HashSet<>
        (Arrays.asList("mid","did")));
    Assert.assertEquals("DID_MID", queryId);

    queryId = reportRequestHelper.deriveQueryIdFromFilters("logistimo", new HashSet<>
        (Arrays.asList("mid","page","did","ktag","cn","size")));
    Assert.assertEquals("DID_KTAG_MID_CN", queryId);

    queryId = reportRequestHelper.deriveQueryIdFromFilters("logistimo", new HashSet<>
        (Arrays.asList("mid","page","did","ktag","cn","size","st")));
    Assert.assertEquals("DID_KTAG_MID_CN_ST", queryId);
  }

  @Test
  public void getQueryRequestModelTest() {
    ReportRequestModel reportRequestModel = new ReportRequestModel();
    reportRequestModel.setUserId("logistimo");
    reportRequestModel.setReportType("some-report");
    reportRequestModel.setReportSubType("");
    Map<String, String> filters = new HashMap<>();
    filters.put("dimension1", "value1");
    filters.put("dimension2", "value2");
    reportRequestModel.setFilters(filters);

    ReportConfig reportConfig = new ReportConfig();
    reportConfig.setUserId("logistimo");
    reportConfig.setType("some-report");
    reportConfig.setSubType("");
    reportConfig.setColumnFilterId("some-report-column-filter-id");
    Map<String, String> metrics = new HashMap<>();
    metrics.put("metric1", "$m1");
    metrics.put("metric2", "$m2");
    metrics.put("metric3", "$$math($mm1+$mm2)$$");
    reportConfig.setMetrics(metrics);



    when(filterService.getFilter("logistimo", "dimension1")).thenReturn(Optional.of
        (getDummyFilterWithPlaceholder("PLACEHOLDER_DIMENSION1")));
    when(filterService.getFilter("logistimo", "dimension2")).thenReturn(Optional.of
        (getDummyFilterWithPlaceholder("PLACEHOLDER_DIMENSION2")));
    when(filterService.getFilter("logistimo", "some-report-column-filter-id")).thenReturn(Optional.of
        (getDummyFilterWithPlaceholder("PLACEHOLDER_COLUMNS")));
    when(queryService.getAllQueryIds("logistimo")).thenReturn(Arrays.asList("dimension1_suffix",
        "dimension2_suffix","dimension1_dimension2_suffix"));

    QueryRequestModel queryRequestModel = reportRequestHelper.getQueryRequestModel
        (reportRequestModel, reportConfig);
    Assert.assertEquals("dimension1_dimension2_suffix", queryRequestModel.queryId);
    Assert.assertEquals(3, queryRequestModel.filters.size());
    Assert.assertEquals("value1", queryRequestModel.filters.get("PLACEHOLDER_DIMENSION1"));
    Assert.assertEquals("value2", queryRequestModel.filters.get("PLACEHOLDER_DIMENSION2"));
    Assert.assertEquals(4, Arrays.asList(StringUtils.split(queryRequestModel.filters.get
            ("PLACEHOLDER_COLUMNS"), ",")).size());
    Assert.assertTrue(Arrays.asList(StringUtils.split(queryRequestModel.filters.get
            ("PLACEHOLDER_COLUMNS"), ",")).contains("m1"));
    Assert.assertTrue(Arrays.asList(StringUtils.split(queryRequestModel.filters.get
            ("PLACEHOLDER_COLUMNS"), ",")).contains("m2"));
    Assert.assertTrue(Arrays.asList(StringUtils.split(queryRequestModel.filters.get
            ("PLACEHOLDER_COLUMNS"), ",")).contains("mm1"));
    Assert.assertTrue(Arrays.asList(StringUtils.split(queryRequestModel.filters.get
            ("PLACEHOLDER_COLUMNS"), ",")).contains("mm2"));
  }

  private Filter getDummyFilterWithPlaceholder(String placeholder) {
    Filter filter = new Filter();
    filter.setPlaceholder(placeholder);
    return filter;
  }
}