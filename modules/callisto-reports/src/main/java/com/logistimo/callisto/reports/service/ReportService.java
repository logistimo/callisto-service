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
import com.logistimo.callisto.function.FunctionUtil;
import com.logistimo.callisto.model.QueryRequestModel;
import com.logistimo.callisto.model.ReportConfig;
import com.logistimo.callisto.reports.core.BuildReportRequestAction;
import com.logistimo.callisto.reports.core.DataXUtils;
import com.logistimo.callisto.reports.core.IReportDataFormatter;
import com.logistimo.callisto.reports.core.IReportQueryBuilder;
import com.logistimo.callisto.reports.core.ReportQueryBuilder;
import com.logistimo.callisto.reports.exception.BadReportRequestException;
import com.logistimo.callisto.reports.exception.DuplicateReportException;
import com.logistimo.callisto.reports.model.DataXReportRequestModel;
import com.logistimo.callisto.reports.model.ReportModel;
import com.logistimo.callisto.reports.model.ReportRequestModel;
import com.logistimo.callisto.reports.model.ReportResult;
import com.logistimo.callisto.repository.ReportConfigRepository;
import com.logistimo.callisto.service.IQueryService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.modelmapper.ModelMapper;
import org.modelmapper.internal.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.util.Version;
import org.springframework.stereotype.Service;

@Service
public class ReportService implements IReportService {

  private ReportConfigRepository reportConfigRepository;
  private ResultManager resultManager;
  private IQueryService queryService;
  private Map<String, IReportQueryBuilder> reportQueryBuilders;
  private IReportDataFormatter reportDataFormatter;
  private ModelMapper modelMapper = new ModelMapper();
  private BuildReportRequestAction buildReportRequestAction;

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
  public void setReportQueryBuilders(Map<String, IReportQueryBuilder> reportQueryBuilders) {
    this.reportQueryBuilders = reportQueryBuilders;
  }

  @Autowired
  @Qualifier("json")
  public void setReportDataFormatter(IReportDataFormatter reportDataFormatter) {
    this.reportDataFormatter = reportDataFormatter;
  }

  @Autowired
  public void setBuildReportRequestAction(BuildReportRequestAction buildReportRequestAction) {
    this.buildReportRequestAction = buildReportRequestAction;
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
  public Optional<ReportModel> getReportModel(String userId, String type, String subType) {
    Optional<ReportConfig> reportConfig = getReportConfig(userId, type, subType);
    return reportConfig.isPresent()
        ? Optional.of(modelMapper.map(reportConfig.get(), ReportModel.class))
        : Optional.empty();
  }

  @Override
  public ReportResult getReportData(ReportRequestModel request) {
    ReportConfig reportConfig =
        getReportConfig(request.getUserId(), request.getType(), request.getSubType())
            .orElseThrow(
                () ->
                    new BadReportRequestException(
                        String.format(
                            "Report %s not configured for user %s!",
                            request.getType(), request.getUserId())));
    request.setReportConfig(reportConfig);
    Pair<QueryRequestModel, QueryResults> reportQueryResult = getReportQueryResult(request);
    QueryResults derivedResults =
        getDerivedResults(request, reportQueryResult.getKey(), reportQueryResult.getValue());
    ReportResult reportResult = new ReportResult();
    reportResult.setUserId(request.getUserId());
    reportResult.setReportType(request.getType());
    reportResult.setReportSubType(request.getSubType());
    reportResult.setResults(
        reportDataFormatter.getFormattedResult(
            request.getUserId(), request.getReportConfig().getMetrics().keySet(), derivedResults));
    return reportResult;
  }

  @Override
  public ReportResult getReportData(DataXReportRequestModel dataXReportRequestModel) {
    ReportRequestModel reportRequestModel =
        buildReportRequestAction.invoke(dataXReportRequestModel);
    Pair<QueryRequestModel, QueryResults> reportQueryResult =
        getReportQueryResult(reportRequestModel);
    final QueryResults results = reportQueryResult.getValue();

    QueryResults flattenedQueryResults = getReportQueryBuilder(reportRequestModel)
        .postProcessQueryResults(results, reportRequestModel);
    ReportResult reportResult = new ReportResult();
    QueryResults derivedResults =
        getDerivedResults(reportRequestModel, reportQueryResult.getKey(), flattenedQueryResults);
    reportResult.setResults(
        reportDataFormatter.getFormattedResult(
            dataXReportRequestModel.getUserId(),
            dataXReportRequestModel.getDerivedMetrics().keySet(),
            derivedResults));
    return reportResult;
  }

  private IReportQueryBuilder getReportQueryBuilder(ReportRequestModel request) {
      String reportVersion =
          matchVersionOfReport(
              request.getReportConfig().getVersion(),
              Lists.from(reportQueryBuilders.keySet().iterator()));
      final IReportQueryBuilder reportQueryBuilder = reportQueryBuilders.get(reportVersion);
      return reportQueryBuilder;
  }

  private Pair<QueryRequestModel, QueryResults> getReportQueryResult(ReportRequestModel request) {
    String reportVersion =
        matchVersionOfReport(
            request.getReportConfig().getVersion(),
            Lists.from(reportQueryBuilders.keySet().iterator()));
    final IReportQueryBuilder reportQueryBuilder = reportQueryBuilders.get(reportVersion);
    QueryRequestModel queryRequestModel =
        reportQueryBuilder.getQueryRequestModel(request, request.getReportConfig());
    return Pair.of(queryRequestModel, queryService.readData(queryRequestModel));
  }

  private QueryResults getDerivedResults(
      ReportRequestModel request, QueryRequestModel queryRequestModel, QueryResults rawResults) {
    Set<String> columns = FunctionUtil.extractColumnSet(request.getReportConfig().getMetrics());
    Map<String, String> derivedColumns =
        resultManager.getCompleteDerivedColumnsMap(
            request.getReportConfig().getMetrics(), columns, rawResults.getHeadings());
    return resultManager.getDerivedResults(queryRequestModel, rawResults, derivedColumns);
  }

  private String matchVersionOfReport(Version reportVersion, List<String> allVersions) {
    allVersions.sort(Comparator.reverseOrder());
    for (String v : allVersions) {
      if (Version.parse(v).isLessThanOrEqualTo(reportVersion)) {
        return v;
      }
    }
    return allVersions.get(0);
  }

  @Override
  public void saveReportConfig(ReportConfig reportConfig) {
    if (StringUtils.isEmpty(reportConfig.getSubType())) {
      List<ReportConfig> reportConfigs =
          reportConfigRepository.findByUserIdAndType(
              reportConfig.getUserId(), reportConfig.getType());
      if (reportConfigs != null
          && reportConfigs.stream().anyMatch(s -> StringUtils.isEmpty(s.getSubType()))) {
        throw new DuplicateReportException("Report with same type and subtype exists!");
      }
    } else if (reportConfigRepository
        .findOneByUserIdAndTypeAndSubType(
            reportConfig.getUserId(), reportConfig.getType(), reportConfig.getSubType())
        .isPresent()) {
      throw new DuplicateReportException("Report with same type and subtype exists!");
    }
    this.reportConfigRepository.save(reportConfig);
  }

  private Optional<ReportConfig> getReportConfig(
      @NonNull String userId, String type, String subType) {
    if (StringUtils.isNotEmpty(type) && StringUtils.isNotEmpty(subType)) {
      return reportConfigRepository.findOneByUserIdAndTypeAndSubType(userId, type, subType);
    } else if (StringUtils.isEmpty(subType)) {
      return reportConfigRepository.findByUserIdAndType(userId, type).stream()
          .filter(reportConfig -> StringUtils.isEmpty(reportConfig.getSubType()))
          .findFirst();
    }
    return Optional.empty();
  }
}
