package com.logistimo.callisto.service;

import com.logistimo.callisto.model.ServerConfig;
import com.logistimo.callisto.model.User;

/** @author Chandrakant */
public interface IUserService {

  User readUser(String userId);

  String saveUser(User user);

  String updateUser(User user); //cannot change userId, but config

  String deleteUser(String userId);

  ServerConfig readServerConfig(String userId, String serverId);
}
