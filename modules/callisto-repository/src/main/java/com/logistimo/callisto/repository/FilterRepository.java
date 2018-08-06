package com.logistimo.callisto.repository;

import com.logistimo.callisto.model.Filter;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FilterRepository extends MongoRepository<Filter, String> {
  List<Filter> findByUserId(String userId);

  @Query(value = "{ 'userId': ?0 , 'filterId': ?1 } ")
  Optional<Filter> findOne(String userId, String filterId);
}