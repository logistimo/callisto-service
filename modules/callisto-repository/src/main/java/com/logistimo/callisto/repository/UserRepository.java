package com.logistimo.callisto.repository;

import com.logistimo.callisto.model.User;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by chandrakant on 08/03/17.
 */
@Repository
public interface UserRepository extends MongoRepository<User, String> {

  @Query(value = "{ 'userId': ?0 }", fields = "{ 'serverConfigs':{ $elemMatch: { 'id': ?1 }}}")
  List<User> readServerConfig(String userId, String serverId, Pageable pageable);

  List<User> findByUserId(String userId);
}
