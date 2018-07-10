package com.logistimo.callisto.service.impl;

import com.logistimo.callisto.exception.CallistoException;
import com.logistimo.callisto.model.Datastore;
import com.logistimo.callisto.repository.DatastoreRepository;
import com.logistimo.callisto.service.IDatastoreService;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by charan on 09/06/18.
 */
@Service
public class DatastoreService implements IDatastoreService {

  private static final Logger logger = Logger.getLogger(DatastoreService.class);

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
      logger.error("Error while saving database", e);
      throw new CallistoException(e);
    }
  }

  @Override
  public Datastore getDatastoreById(String datastoreId) {
    return repository.findOne(datastoreId);
  }

}
