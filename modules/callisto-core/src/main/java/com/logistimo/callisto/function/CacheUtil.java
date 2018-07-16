/*
 * Copyright © 2017 Logistimo.
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

package com.logistimo.callisto.function;

import com.logistimo.callisto.CharacterConstants;
import com.logistimo.callisto.QueryResults;
import com.logistimo.callisto.exception.CallistoException;
import com.logistimo.callisto.service.IQueryService;

import org.apache.log4j.Logger;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.Map;


@Component
public class CacheUtil {
  private static final Logger logger = Logger.getLogger(CacheUtil.class);

  @Cacheable(value = "links", key = "#cacheKey")
  public String getLinkResult(LinkFunction linkFunction, FunctionParam functionParam, String
      queryId, Map<String, String> linkFiltersMap, IQueryService queryService, int cacheKey)
      throws CallistoException {
    if (linkFiltersMap != null && !linkFiltersMap.isEmpty()) {
      functionParam.getQueryRequestModel().filters.putAll(linkFiltersMap);
    }
    QueryResults rs = queryService
        .readData(
            linkFunction.buildQueryRequestModel(functionParam.getQueryRequestModel(), queryId));
    if (rs.getRows() != null && rs.getRows().size() == 1 && rs.getRows().get(0).size() == 1) {
      return rs.getRows().get(0).get(0);
    }
    logger.warn("Expected result size from Link function " + functionParam.function
                + " is 1. Actual result: " + rs.toString());
    return CharacterConstants.EMPTY;
  }

}