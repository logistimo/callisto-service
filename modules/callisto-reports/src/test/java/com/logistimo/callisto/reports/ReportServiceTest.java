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

import com.logistimo.callisto.QueryResults;
import com.logistimo.callisto.ResultManager;
import com.logistimo.callisto.function.FunctionUtil;
import com.logistimo.callisto.model.QueryRequestModel;
import com.logistimo.callisto.model.ReportConfig;
import com.logistimo.callisto.reports.core.IReportDataFormatter;
import com.logistimo.callisto.reports.core.IReportQueryBuilder;
import com.logistimo.callisto.reports.core.ReportQueryBuilder;
import com.logistimo.callisto.reports.exception.BadReportRequestException;
import com.logistimo.callisto.reports.model.ReportModel;
import com.logistimo.callisto.reports.model.ReportRequestModel;
import com.logistimo.callisto.reports.service.ReportService;
import com.logistimo.callisto.repository.ReportConfigRepository;
import com.logistimo.callisto.service.IQueryService;
import com.logistimo.callisto.service.impl.QueryService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ReportServiceTest {

  private ReportService reportService;
  private ReportConfigRepository reportConfigRepository;
  private ResultManager resultManager;
  private IQueryService queryService;
  private IReportQueryBuilder reportQueryBuilder;
  private IReportDataFormatter reportDataFormatter;

  @Before
  public void setup() {
    reportService = new ReportService();
    reportConfigRepository = mock(ReportConfigRepository.class);
    reportService.setReportConfigRepository(reportConfigRepository);
    resultManager = mock(ResultManager.class);
    reportService.setResultManager(resultManager);
    queryService = mock(QueryService.class);
    reportService.setQueryService(queryService);
    reportQueryBuilder = mock(ReportQueryBuilder.class);
    HashMap<String, IReportQueryBuilder> reportQueryBuilderHashMap = new HashMap<>();
    reportQueryBuilderHashMap.put("1.0.0", reportQueryBuilder);
    reportService.setReportQueryBuilders(reportQueryBuilderHashMap);
    reportDataFormatter = mock(IReportDataFormatter.class);
    reportService.setReportDataFormatter(reportDataFormatter);
  }

  @Test
  public void getAllReportsTest() {
    ReportConfig r1 = new ReportConfig();
    r1.setType("report1");
    r1.setSubType("overview");
    ReportConfig r2 = new ReportConfig();
    r2.setType("report2");
    r2.setSubType("by_stores");
    when(reportConfigRepository.readAllReportTypes(eq("logistimo")))
        .thenReturn(Arrays.asList(r1, r2));
    List<ReportModel> reports = reportService.getAllReports("logistimo");
    verify(reportConfigRepository, times(1)).readAllReportTypes(eq("logistimo"));
    assertEquals(2, reports.size());
    assertEquals("report1", reports.get(0).getType());
    assertEquals("overview", reports.get(0).getSubType());
    assertEquals("report2", reports.get(1).getType());
    assertEquals("by_stores", reports.get(1).getSubType());
  }

  @Test(expected = BadReportRequestException.class)
  public void getReportDataReportNotFound1Test() {
    ReportRequestModel reportRequestModel = new ReportRequestModel();
    reportRequestModel.setUserId("logistimo");
    reportRequestModel.setType("r1");
    reportRequestModel.setSubType("overview");
    when(reportConfigRepository
        .findOneByUserIdAndTypeAndSubType(eq("logistimo"), eq("r1"), eq("overview")))
        .thenReturn(Optional.empty());
    reportService.getReportData(reportRequestModel);
  }

  @Test(expected = BadReportRequestException.class)
  public void getReportDataReportNotFound2Test() {
    ReportConfig r1 = new ReportConfig();
    r1.setType("r1");
    r1.setSubType("overview");
    ReportConfig r2 = new ReportConfig();
    r2.setType("r1");
    r2.setSubType("by_stores");
    ReportRequestModel reportRequestModel = new ReportRequestModel();
    reportRequestModel.setUserId("logistimo");
    reportRequestModel.setType("r1");
    reportRequestModel.setSubType("");
    when(reportConfigRepository
        .findByUserIdAndType(eq("logistimo"), eq("r1")))
        .thenReturn(Arrays.asList(r1, r2));
    reportService.getReportData(reportRequestModel);
  }

  @Test
  public void getReportDataTest() {
    ReportRequestModel reportRequestModel = new ReportRequestModel();
    reportRequestModel.setUserId("logistimo");
    reportRequestModel.setType("r1");
    reportRequestModel.setSubType("overview");
    ReportConfig reportConfig = new ReportConfig();
    reportConfig.setType("r1");
    reportConfig.setSubType("overview");
    reportConfig.setUserId("logistimo");
    Map<String, String> metrics = new HashMap<>();
    metrics.put("metric1", "$met");
    reportConfig.setMetrics(metrics);
    when(reportConfigRepository
        .findOneByUserIdAndTypeAndSubType(eq("logistimo"), eq("r1"), eq("overview")))
        .thenReturn(Optional.of(reportConfig));
    QueryRequestModel queryRequestModel = new QueryRequestModel();
    queryRequestModel.userId = "logistimo";
    queryRequestModel.queryId = "select some-data from some-table";
    when(reportQueryBuilder.getQueryRequestModel(reportRequestModel, reportConfig))
        .thenReturn(queryRequestModel);
    QueryResults rs = mock(QueryResults.class);
    when(queryService.readData(eq(queryRequestModel))).thenReturn(rs);
    when(FunctionUtil.extractColumnSet(reportConfig.getMetrics()))
        .thenReturn(new HashSet<>(Collections.singletonList("met")));
    Map<String, String> completeDerivedMap =
        (Map<String, String>) ((HashMap)reportConfig.getMetrics()).clone();
    completeDerivedMap.put("met", "$met");
    when(resultManager.getCompleteDerivedColumnsMap(anyMap(), anySet(), anyList()))
        .thenReturn(completeDerivedMap);
    when(resultManager.getDerivedResults(eq(queryRequestModel), eq(rs), eq(completeDerivedMap)))
        .thenReturn(mock(QueryResults.class));
    reportService.getReportData(reportRequestModel);
    verify(reportDataFormatter, times(1)).getFormattedResult(eq("logistimo"), anySet(), any());
  }
}
