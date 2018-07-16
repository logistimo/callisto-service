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

package com.logistimo.callisto.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Mohan Raja
 */
public class QueryRequestModel {
  public String userId = "logistimo";
  public String queryId;
  public QueryText query; // used only if queryId is not present
  public Map<String, String> filters;
  public String derivedResultsId; //constant ID for the desired result i.e. LinkedHashMap
  public Map<String, String> columnText;
  public Integer size;
  public List<String> rowHeadings;
  public Integer offset;

  public QueryRequestModel() {

  }

  public QueryRequestModel(QueryRequestModel queryRequestModel) throws CloneNotSupportedException {
    this.userId = String.valueOf(queryRequestModel.userId);
    this.queryId = queryRequestModel.queryId;
    this.query = queryRequestModel.query;
    this.filters = queryRequestModel.filters == null ? null : (Map) ((HashMap) queryRequestModel.filters).clone();
    this.columnText =
        queryRequestModel.columnText == null ? null : (Map) ((HashMap) queryRequestModel.columnText).clone();
    this.rowHeadings =
        queryRequestModel.rowHeadings == null ? null : (List) ((ArrayList) queryRequestModel.rowHeadings).clone();
    this.derivedResultsId = queryRequestModel.derivedResultsId;
    this.size = queryRequestModel.size;
    this.offset = queryRequestModel.offset;
  }

}
