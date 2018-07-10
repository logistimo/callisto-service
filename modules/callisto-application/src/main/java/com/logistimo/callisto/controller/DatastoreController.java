package com.logistimo.callisto.controller;

import com.logistimo.callisto.exception.CallistoException;
import com.logistimo.callisto.model.Datastore;
import com.logistimo.callisto.service.IDatastoreService;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import javax.annotation.Resource;

/**
 * Created by charan on 09/06/18.
 */
@RestController
@RequestMapping("/datastore")
public class DatastoreController {

  @Resource
  IDatastoreService datastoreService;

  @RequestMapping(value = "", method = RequestMethod.GET)
  public List<Datastore> getDatastoresByUser(@RequestParam String userId)
      throws CallistoException {
    return datastoreService.getDatastoresByUser(userId);
  }

  @RequestMapping(value = "", method = RequestMethod.POST)
  public void createOrUpdateDatastore(@RequestBody Datastore model)
      throws CallistoException {
    datastoreService.save(model);
  }

}
