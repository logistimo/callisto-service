package com.logistimo.callisto.controller;

import com.logistimo.callisto.model.User;
import com.logistimo.callisto.service.IUserService;

import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/** @author Chandrakant */
@RestController
@RequestMapping("/user")
public class UserController {
  @Resource IUserService userService;

  @RequestMapping(value = "/get", method = RequestMethod.GET)
  public @ResponseBody User getUser(@RequestParam(defaultValue = "logistimo") String userId) {
    return userService.readUser(userId);
  }

  @RequestMapping(value = "/register", method = RequestMethod.POST)
  public String register(@RequestBody User data, HttpServletRequest request) {
    String response = null;
    if (StringUtils.isNotEmpty(data.getUserId())) {
      response = userService.saveUser(data);
    }
    return response;
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
