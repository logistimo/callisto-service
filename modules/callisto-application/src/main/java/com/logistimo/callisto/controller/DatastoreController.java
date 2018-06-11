package com.logistimo.callisto.controller;

import com.logistimo.callisto.service.IDatastoreService;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * Created by charan on 09/06/18.
 */
@RestController
@RequestMapping("/datastore")
public class DatastoreController {

  @Resource
  IDatastoreService datastoreService;

}
