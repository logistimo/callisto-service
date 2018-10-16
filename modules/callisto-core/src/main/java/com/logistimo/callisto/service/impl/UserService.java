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

package com.logistimo.callisto.service.impl;

import com.logistimo.callisto.exception.CallistoException;
import com.logistimo.callisto.model.User;
import com.logistimo.callisto.repository.UserRepository;
import com.logistimo.callisto.service.IUserService;
import com.mongodb.DuplicateKeyException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * @author Chandrakant
 * @author Mohan Raja
 */
@Service
public class UserService implements IUserService {

  private static final Logger logger = LoggerFactory.getLogger(UserService.class);

  private UserRepository repository;

  @Autowired
  public void setUserRepository(UserRepository repository) {
    this.repository = repository;
  }

  public Optional<User> readUser(String userId) {
    return repository.findOneByUserId(userId);
  }

  public void saveUser(User user) {
    try {
      repository.insert(user);
    } catch (DuplicateKeyException e) {
      logger.warn("Duplicate userId", e);
    } catch (Exception e) {
      logger.error("Error while creating user", e);
    }
  }

  public String updateUser(User user) {
    String res = CallistoException.RESULT_FAILURE;
    try {
      Optional<User> userDb = repository.findOneByUserId(user.getUserId());
      if (userDb.isPresent()) {
        user.setId(userDb.get().getId());
        repository.save(user);
        res = "User successfully updated";
      }
    } catch (Exception e) {
      logger.error("Error while updating user", e);
    }
    return res;
  }

  public String deleteUser(String userId) {
    String res = CallistoException.RESULT_FAILURE;
    try {
      Optional<User> userDb = repository.findOneByUserId(userId);
      if (userDb.isPresent()) {
        repository.delete(userDb.get());
        res = "success";
      }
    } catch (Exception e) {
      logger.error("Error while deleting user for userId " + userId, e);
    }
    return res;
  }
}
