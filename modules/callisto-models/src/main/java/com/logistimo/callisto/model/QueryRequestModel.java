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
public class QueryRequestModel implements Cloneable {
  public String userId = "logistimo";
  public String queryId;
  public QueryText query;
  public Map<String, String> filters;
  public String derivedResultsId; //constant ID for the desired result i.e. LinkedHashMap
  public Map<String, String> columnText;
  public Integer size;
  public List<String> rowHeadings;
  public Integer offset;

  @Override
  public QueryRequestModel clone() {
    QueryRequestModel newRequestModel = new QueryRequestModel();
    newRequestModel.userId = String.valueOf(this.userId);
    newRequestModel.queryId = this.queryId;
    newRequestModel.filters = this.filters == null ? null : (Map) ((HashMap) this
        .filters)
        .clone();
    newRequestModel.columnText =
        this.columnText == null ? null : (Map) ((HashMap) this.columnText).clone();
    newRequestModel.rowHeadings =
        this.rowHeadings == null ? null : (List) ((ArrayList) this.rowHeadings).clone();
    newRequestModel.derivedResultsId = this.derivedResultsId;
    newRequestModel.size = this.size;
    newRequestModel.offset = this.offset;
    return newRequestModel;
  }

}
