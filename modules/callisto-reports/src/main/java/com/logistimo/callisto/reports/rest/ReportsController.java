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

import com.logistimo.callisto.reports.ReportRequestModel;
import com.logistimo.callisto.reports.exception.BadReportRequestException;
import com.logistimo.callisto.reports.model.ReportModel;
import com.logistimo.callisto.reports.model.ReportResult;
import com.logistimo.callisto.reports.service.IReportService;

import org.apache.commons.lang.StringUtils;
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

@RestController
@RequestMapping("/reports")
public class ReportsController {

  private IReportService reportService;

  @Autowired
  public void setReportService(IReportService reportService) {
    this.reportService = reportService;
  }

  @RequestMapping(value = "", method = RequestMethod.GET)
  public ResponseEntity getAllReportTypes(@RequestHeader(value = "User-Id", defaultValue =
      "logistimo") String userId) {
    List<ReportModel> list = reportService.getAllReports(userId);
    return new ResponseEntity<>(list, HttpStatus.OK);
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
    if (StringUtils.isNotEmpty(type)) {
      reportRequestModel.setType(type);
      reportRequestModel.setUserId(userId);
    }
    ReportResult result = reportService.getReportData(reportRequestModel);
    return new ResponseEntity<>(result, HttpStatus.OK);
  }
}