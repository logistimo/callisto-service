/*
 * Copyright © 2018 Logistimo.
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

import com.logistimo.callisto.exception.CallistoException;
import com.logistimo.callisto.model.ApiResponse;
import com.logistimo.callisto.model.Datastore;
import com.logistimo.callisto.model.SuccessResponseDetails;
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

/**
 * Created by charan on 09/06/18.
 */
@RestController
@RequestMapping("/datastore")
public class DatastoreController {

  @Resource
  private IDatastoreService datastoreService;

  @RequestMapping(value = "", method = RequestMethod.GET)
  public ResponseEntity getDatastoresByUser(@RequestParam String userId)
      throws CallistoException {
    List<Datastore> datastores = datastoreService.getDatastoresByUser(userId);
    return new ResponseEntity<>(new ApiResponse<>(datastores),
        datastores.isEmpty() ? HttpStatus.NO_CONTENT : HttpStatus.OK);
  }

  @RequestMapping(value = "/{datastoreId}", method = RequestMethod.GET)
  public ResponseEntity getDatastore(@PathVariable String datastoreId,
                                     @RequestParam String userId) throws CallistoException {
    Datastore datastore = datastoreService.getDatastoreById(datastoreId);
    return new ResponseEntity<>(datastore, HttpStatus.OK);
  }

  @RequestMapping(value = "", method = RequestMethod.PUT)
  public ResponseEntity createOrUpdateDatastore(@RequestBody Datastore model) {
    datastoreService.save(model);
    SuccessResponseDetails successResponseDetails = new SuccessResponseDetails("Datastore updated successfully");
    return new ResponseEntity<>(successResponseDetails, HttpStatus.OK);
  }

}
