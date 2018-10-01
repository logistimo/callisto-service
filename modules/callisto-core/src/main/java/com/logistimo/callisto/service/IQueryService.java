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

package com.logistimo.callisto.service;

import com.logistimo.callisto.ResultManager;
import com.logistimo.callisto.exception.CallistoException;
import com.logistimo.callisto.QueryResults;
import com.logistimo.callisto.model.PagedResults;
import com.logistimo.callisto.model.QueryRequestModel;
import com.logistimo.callisto.model.QueryText;

import org.springframework.data.domain.Pageable;
import java.util.List;

/** @author Chandrakant */
public interface IQueryService {

  void saveQuery(QueryText q);

  void updateQuery(QueryText q);

  QueryText readQuery(String userId, String queryId);

  List<String> readQueryIds(String userId, String like, Pageable pageable);

  QueryResults readData(QueryRequestModel requestModel)
      throws CallistoException;

  QueryResults readAndModifyData(QueryRequestModel requestModel, ResultManager resultManager);

  void deleteQuery(String userId, String queryId);

  List<String> getAllQueryIds(String userId);

  List<QueryText> readQueries(String userId, Pageable pageable);

  Long getTotalNumberOfQueries(String userId);

  PagedResults searchQueriesLike(String userId, String like, Pageable pageable);
}
