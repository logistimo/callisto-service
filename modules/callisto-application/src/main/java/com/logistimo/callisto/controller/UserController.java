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

package com.logistimo.callisto.controller;

import com.logistimo.callisto.model.User;
import com.logistimo.callisto.service.IUserService;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

/** @author Chandrakant */
@RestController
@RequestMapping("/user")
public class UserController {

  @Autowired
  IUserService userService;

  @RequestMapping(value = "/get", method = RequestMethod.GET)
  public @ResponseBody ResponseEntity getUser(@RequestParam(defaultValue = "logistimo") String userId) {
    Optional<User> user = userService.readUser(userId);
    return user.isPresent() ?
        new ResponseEntity<>(user.get(), HttpStatus.OK) :
        new ResponseEntity(HttpStatus.NOT_FOUND);
  }

  @RequestMapping(value = "/register", method = RequestMethod.POST)
  public ResponseEntity register(@RequestBody User data, HttpServletRequest request) {
    if (StringUtils.isNotEmpty(data.getUserId())) {
      userService.saveUser(data);
    }
    return new ResponseEntity(HttpStatus.OK);
  }

  @RequestMapping(value = "/update", method = RequestMethod.POST)
  public String updateUser(@RequestBody User data) {
    String response = null;
    if (StringUtils.isNotEmpty(data.getUserId())) {
      response = userService.updateUser(data);
    }
    return response;
  }

  @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
  public String deleteUser(@RequestParam String userId) {
    String response = null;
    if (userId != null) {
      response = userService.deleteUser(userId);
    }
    return response;
  }
}
