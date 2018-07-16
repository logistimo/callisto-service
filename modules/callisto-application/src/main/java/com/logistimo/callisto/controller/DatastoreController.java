package com.logistimo.callisto.controller;

import com.logistimo.callisto.SuccessDetails;
import com.logistimo.callisto.exception.CallistoException;
import com.logistimo.callisto.model.Datastore;
import com.logistimo.callisto.service.IDatastoreService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import javax.annotation.Resource;
import javax.xml.crypto.Data;

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

  @RequestMapping(value = "/{datastoreId}", method = RequestMethod.GET)
  public ResponseEntity getDatastore(@PathVariable String datastoreId, @RequestParam String
      userId)
      throws CallistoException {
    Datastore datastore = datastoreService.getDatastoreById(datastoreId);
    return new ResponseEntity<>(datastore, HttpStatus.OK);
  }

  @RequestMapping(value = "", method = RequestMethod.PUT)
  public ResponseEntity createOrUpdateDatastore(@RequestBody Datastore model) {
    datastoreService.save(model);
    SuccessDetails successDetails = new SuccessDetails("Datastore updated successfully");
    return new ResponseEntity<>(successDetails, HttpStatus.OK);
  }

}
