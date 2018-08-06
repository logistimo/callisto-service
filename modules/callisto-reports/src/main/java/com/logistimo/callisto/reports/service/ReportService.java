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

package com.logistimo.callisto.reports.service;

import com.logistimo.callisto.QueryResults;
import com.logistimo.callisto.ResultManager;
import com.logistimo.callisto.model.QueryRequestModel;
import com.logistimo.callisto.model.ReportConfig;
import com.logistimo.callisto.reports.ReportRequestModel;
import com.logistimo.callisto.reports.core.ReportDataHelper;
import com.logistimo.callisto.reports.core.ReportRequestHelper;
import com.logistimo.callisto.reports.model.ReportModel;
import com.logistimo.callisto.reports.model.ReportResult;
import com.logistimo.callisto.repository.ReportConfigRepository;
import com.logistimo.callisto.service.IQueryService;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ReportService implements IReportService {

  private ReportConfigRepository reportConfigRepository;
  private ResultManager resultManager;
  private IQueryService queryService;
  private ReportRequestHelper reportRequestHelper;
  private ReportDataHelper reportDataHelper;
  private ModelMapper modelMapper = new ModelMapper();

  @Autowired
  public void setReportConfigRepository(ReportConfigRepository reportConfigRepository) {
    this.reportConfigRepository = reportConfigRepository;
  }

  @Autowired
  public void setResultManager(ResultManager resultManager) {
    this.resultManager = resultManager;
  }

  @Autowired
  public void setQueryService(IQueryService queryService) {
    this.queryService = queryService;
  }

  @Autowired
  public void setReportRequestHelper(ReportRequestHelper reportRequestHelper) {
    this.reportRequestHelper = reportRequestHelper;
  }

  @Autowired
  public void setReportDataHelper(ReportDataHelper reportDataHelper) {
    this.reportDataHelper = reportDataHelper;
  }

  @Override
  public List<ReportModel> getAllReports(String userId) {
    List<ReportConfig> reportConfigs = reportConfigRepository.readAllReportTypes(userId);
    List<ReportModel> reportModels = new ArrayList<>(reportConfigs.size());
    for (ReportConfig reportConfig : reportConfigs) {
      reportModels.add(modelMapper.map(reportConfig, ReportModel.class));
    }
    return reportModels;
  }

  @Override
  public ReportResult getReportData(String userId, ReportRequestModel reportRequestModel) {
    ReportConfig reportConfig = reportConfigRepository.findOneByUserIdAndTypeAndSubType(userId,
        reportRequestModel.getReportType(), reportRequestModel.getReportSubType());
    QueryRequestModel queryRequestModel = reportRequestHelper
        .getQueryRequestModel(reportRequestModel, reportConfig);
    QueryResults rawResults = queryService.readData(queryRequestModel);
    Set<String> columns = reportRequestHelper.getColumnsFromMetrics(reportConfig.getMetrics());
    Map<String, String> derivedColumns = resultManager.getCompleteDerivedColumnsMap
        (reportConfig.getMetrics(), columns, rawResults.getHeadings());
    QueryResults derivedResults = resultManager.getDerivedResults(queryRequestModel, rawResults,
        derivedColumns);
    ReportResult reportResult = new ReportResult();
    reportResult.setUserId(userId);
    reportResult.setReportType(reportRequestModel.getReportType());
    reportResult.setReportSubType(reportRequestModel.getReportSubType());
    reportResult.setResults(reportDataHelper.formatReportData(userId, reportConfig.getMetrics()
        .keySet(), derivedResults));
    return reportResult;
  }

}