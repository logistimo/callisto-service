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

package com.logistimo.callisto.reports.rest;

import com.logistimo.callisto.model.Filter;
import com.logistimo.callisto.model.ReportConfig;
import com.logistimo.callisto.reports.ReportRequestModel;
import com.logistimo.callisto.reports.exception.BadReportRequestException;
import com.logistimo.callisto.reports.model.ReportModel;
import com.logistimo.callisto.reports.model.ReportResult;
import com.logistimo.callisto.reports.model.SuccessResponseDetails;
import com.logistimo.callisto.reports.service.IReportService;
import com.logistimo.callisto.service.IFilterService;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/reports")
public class ReportsController {

  private IReportService reportService;
  private IFilterService filterService;

  @Autowired
  public void setReportService(IReportService reportService) {
    this.reportService = reportService;
  }

  @Autowired
  public void setFilterService(IFilterService filterService) {
    this.filterService = filterService;
  }

  @RequestMapping(value = "/add", method = RequestMethod.POST)
  public ResponseEntity saveReport(@RequestHeader(value = "User-Id", defaultValue = "logistimo") String userId,
                                   @RequestBody ReportConfig reportConfig) {
    reportConfig.setUserId(userId);
    reportService.saveReportConfig(reportConfig);
    SuccessResponseDetails success = new SuccessResponseDetails("Report saved successfully");
    return new ResponseEntity<>(success, HttpStatus.OK);
  }

  @RequestMapping(value = "", method = RequestMethod.GET)
  public ResponseEntity getAllReportTypes(@RequestHeader(value = "User-Id", defaultValue =
      "logistimo") String userId) {
    List<ReportModel> list = reportService.getAllReports(userId);
    return new ResponseEntity<>(list, HttpStatus.OK);
  }

  @RequestMapping(value = "/{type}", method = RequestMethod.GET)
  public ResponseEntity getReportModel(@RequestHeader(value = "User-Id", defaultValue =
      "logistimo") String userId, @PathVariable String type) {
    return getReportModel(userId, type, null);
  }

  @RequestMapping(value = "/{type}/{subtype}", method = RequestMethod.GET)
  public ResponseEntity getReportModel(@RequestHeader(value = "User-Id", defaultValue =
      "logistimo") String userId, @PathVariable String type, @PathVariable String subtype) {
    Optional<ReportModel> reportModel = reportService.getReportModel(userId, type, subtype);
    return reportModel.isPresent() ?
        new ResponseEntity<>(reportModel.get(), HttpStatus.OK)
        : new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  @RequestMapping(value = "/{type}", method = RequestMethod.POST)
  public ResponseEntity getResults(
      @RequestHeader(value = "User-Id", defaultValue = "logistimo") String userId,
      @RequestBody ReportRequestModel reportRequestModel,
      @PathVariable String type) {
    if(reportRequestModel == null) {
      throw new BadReportRequestException("Report request model not found");
    } else if(reportRequestModel.getFilters() == null) {
      throw new BadReportRequestException("Report filters not found");
    }
    reportRequestModel.getFilters().entrySet().stream()
        .forEach(filterEntry -> {
          Optional<Filter> filter = filterService.getFilter(userId, filterEntry.getKey());
          if (!filter.isPresent()) {
            throw new BadReportRequestException(String.format("Report filter '%s' not registered!",
                filterEntry.getKey()));
          }
        });
    if (StringUtils.isNotEmpty(type)) {
      reportRequestModel.setType(type);
      reportRequestModel.setUserId(userId);
    }
    ReportResult result = reportService.getReportData(reportRequestModel);
    return new ResponseEntity<>(result, HttpStatus.OK);
  }
}