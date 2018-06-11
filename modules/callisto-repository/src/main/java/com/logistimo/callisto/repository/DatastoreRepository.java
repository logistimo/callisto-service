package com.logistimo.callisto.repository;

import com.logistimo.callisto.model.Datastore;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by charan on 09/06/18.
 */
@Repository
public interface DatastoreRepository extends MongoRepository<Datastore, String> {

  @Query(value = "{ 'userId': ?0 , 'id': ?1}")
  List<Datastore> readDatastore(String userId, String datastoreId, Pageable pageable);

  @Query(value = "{ 'userId': ?0}")
  List<Datastore> findByUserId(String userId);

}
