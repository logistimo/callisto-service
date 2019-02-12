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

package com.logistimo.callisto.service.impl;

import com.logistimo.callisto.exception.CallistoException;
import com.logistimo.callisto.model.Datastore;
import com.logistimo.callisto.repository.DatastoreRepository;
import com.logistimo.callisto.service.IDatastoreService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by charan on 09/06/18.
 */
@Service
public class DatastoreService implements IDatastoreService {

  private static final Logger logger = LoggerFactory.getLogger(DatastoreService.class);

  @Autowired private DatastoreRepository repository;

  public Datastore get(String userId, String datastoreId) {
    Datastore datastore = null;
    try {
      List<Datastore>
          datastores =
          repository.readDatastore(userId, datastoreId, new PageRequest(0, 1));
      if (datastores != null && datastores.size() > 0) {
        datastore = datastores.get(0);
      } else {
        logger.warn("Datastore " + datastoreId + " not found");
      }
    } catch (Exception e) {
      logger.error(
          "Error while reading server config for userId " + userId + " and serverId " + datastoreId,
          e);
    }
    return datastore;
  }

  @Override
  public List<Datastore> getDatastoresByUser(String userId){
    return repository.findByUserId(userId);
  }

  @Override
  public void save(Datastore datastore) {
    try {
      Datastore dbDataStore = get(datastore.getUserId(), datastore.getId());
      if (dbDataStore != null) {
        dbDataStore.copyFrom(datastore);
        repository.save(dbDataStore);
      } else {
        repository.save(datastore);
      }
    } catch (Exception e) {
      throw new CallistoException(e);
    }
  }

  @Override
  public Datastore getDatastoreById(String datastoreId) {
    return repository.findOne(datastoreId);
  }

}
