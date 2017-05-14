package com.logistimo.callisto.service.impl;

import com.logistimo.callisto.model.ServerConfig;
import com.logistimo.callisto.model.User;
import com.logistimo.callisto.repository.UserRepository;
import com.logistimo.callisto.service.IUserService;
import com.mongodb.DuplicateKeyException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Chandrakant
 * @author Mohan Raja
 */
@Service
public class UserService implements IUserService {

  private static final Logger logger = Logger.getLogger(UserService.class);

  @Autowired private UserRepository repository;

  public User readUser(String userId) {
    User user = null;
    try {
      List<User> userList = repository.findByUserId(userId);
      if (userList != null && userList.size() == 1) {
        user = userList.get(0);
      }
    } catch (Exception e) {
      logger.error("Error while reading user " + userId, e);
    }
    return user;
  }

  public String saveUser(User user) {
    String res = "failure";
    try {
      repository.insert(user);
      res = "success";
    } catch (DuplicateKeyException e) {
      System.out.println("Duplicate userId");
      return "Duplicate userId";
    } catch (Exception e) {
      logger.error("Error while creating user", e);
    }
    return res;
  }

  public String updateUser(User user) {
    String res = "failure";
    try {
      List<User> userList = repository.findByUserId(user.getUserId());
      if (userList != null && userList.size() == 1) {
        user.setId(userList.get(0).getId());
        repository.save(user);
        res = "User successfully updated";
      }
    } catch (Exception e) {
      logger.error("Error while updating user", e);
    }
    return res;
  }

  public ServerConfig readServerConfig(String userId, String serverId) {
    ServerConfig serverConfig = null;
    try {
      List<User> users = repository.readServerConfig(userId, serverId, new PageRequest(0, 1));
      if (users != null && users.size() == 1) {
        List<ServerConfig> serverConfigs = users.get(0).getServerConfigs();
        if (serverConfigs != null) {
          serverConfig = serverConfigs.get(0);
        }
      } else {
        System.out.println("User not found");
      }
    } catch (Exception e) {
      logger.error(
          "Error while reading server config for userId "+ userId +" and serverId " + serverId, e);
    }
    return serverConfig;
  }

  public String deleteUser(String userId) {
    String res = "failure";
    try {
      List<User> userList = repository.findByUserId(userId);
      if (userList != null && userList.size() == 1) {
        repository.delete(userList.get(0));
        res = "success";
      }
    } catch (Exception e) {
      logger.error("Error while deleting user for userId " + userId, e);
    }
    return res;
  }
}
