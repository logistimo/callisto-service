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

package com.logistimo.callisto.repository;

import com.logistimo.callisto.model.QueryText;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Created by chandrakant on 09/03/17.
 */
@Repository
public interface QueryRepository extends MongoRepository<QueryText, String> {

  @Query(value = "{ 'userId': ?0 , 'queryId': ?1 } ")
  Optional<QueryText> findOne(String userId, String queryId);

  @Query(value = "{ 'userId': ?0 , 'queryId': ?1 } ")
  Page<QueryText> readQuery(String userId, String queryId, Pageable pageable);

  @Query(value = "{ 'userId': ?0 } ", fields = "{ 'queryId' : 1 }")
  List<QueryText> readQueryIds(String userId);

  @Query(value = "{ 'userId': ?0 } ", fields = "{ 'queryId' : 1 }")
  List<QueryText> readQueryIds(String userId, Pageable pageable);

  @Query(value = "{ 'userId': ?0 , 'queryId': {$regex : ?1, $options: 'i'} } "
      , fields = "{ 'queryId' : 1 }")
  List<QueryText> readQueryIds(String userId, String like);

  @Query(value = "{ 'userId': ?0 , 'queryId': {$regex : ?1, $options: 'i'}} ", fields = "{ "
                                                                                        + "'queryId' : 1 }")
  List<QueryText> readQueryIds(String userId, String like, Pageable pageable);

  @Query(value = "{ 'userId': ?0 }")
  List<QueryText> readQueries(String userId, Pageable pageable);

  @Query(value = "{ 'userId': ?0 }", count = true)
  Long getCount(String userId);

  @Query(value = "{ 'userId': ?0 , 'queryId': {$regex : ?1, $options: 'i'} }")
  List<QueryText> searchQueriesWithQueryId(String userId, String like, Pageable pageable);

  @Query(value = "{ 'userId': ?0 , 'queryId': {$regex : ?1, $options: 'i'} }", fields = "{ 'queryId' : 1 }", count = true)
  Long getSearchQueriesCount(String userId, String like);
}
