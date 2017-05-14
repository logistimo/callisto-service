package com.logistimo.callisto.repository;

import com.logistimo.callisto.model.QueryText;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/** Created by chandrakant on 09/03/17. */
@Repository
public interface QueryRepository extends MongoRepository<QueryText, String> {

  @Query(value = "{ 'userId': ?0 , 'queryId': ?1 } ")
  List<QueryText> readQuery(String userId, String queryId);

  @Query(value = "{ 'userId': ?0 , 'queryId': ?1 } ")
  List<QueryText> readQuery(String userId, String queryId, Pageable pageable);

  @Query(value = "{ 'userId': ?0 } ", fields = "{ 'queryId' : 1 }")
  List<QueryText> readQueryIds(String userId);
}
