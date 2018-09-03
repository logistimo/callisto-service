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

import com.logistimo.callisto.ICallistoFunction;
import com.logistimo.callisto.function.FunctionParam;
import com.logistimo.callisto.function.LinkFunction;
import com.logistimo.callisto.model.Filter;
import com.logistimo.callisto.model.QueryRequestModel;
import com.logistimo.callisto.service.IFilterService;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public abstract class ReportDataFormatter implements IReportDataFormatter {

  private ICallistoFunction linkFunction;
  private IFilterService filterService;
  private static final Logger logger = Logger.getLogger(ReportDataFormatter.class);

  @Autowired
  public void setFilterService(IFilterService filterService) {
    this.filterService = filterService;
  }

  @Autowired
  @Qualifier("link")
  public void setLinkFunction(ICallistoFunction callistoFunction) {
    this.linkFunction = callistoFunction;
  }

  boolean isRenameQueryPresent(String userId, String key) {
    Optional<Filter> filter = filterService.getFilter(userId, key);
    if(filter.isPresent()) {
      String renameQueryId = filter.get().getRenameQueryId();
      return StringUtils.isNotEmpty(renameQueryId);
    }
    return false;
  }

  String getRenamedValue(String userId, String key, String value) {
    if(isRenameQueryPresent(userId, key)) {
      try {
        Optional<Filter> filter = filterService.getFilter(userId, key);
        String renameQueryId = filter.get().getRenameQueryId();
        if (StringUtils.isNotEmpty(renameQueryId)) {
          FunctionParam functionParam = new FunctionParam();
          Map<String, String> filters = new HashMap<>();
          filters.put(filter.get().getPlaceholder(), value);
          QueryRequestModel queryRequestModel = new QueryRequestModel();
          queryRequestModel.userId = userId;
          queryRequestModel.filters = filters;
          functionParam.setQueryRequestModel(queryRequestModel);
          functionParam.function = LinkFunction.getFunctionSyntax(renameQueryId);
          return linkFunction.getResult(functionParam);
        }
      } catch (Exception e) {
        logger.warn(String.format("Error while renaming type %s with value %s", key, value));
      }
    }
    return null;
  }

}