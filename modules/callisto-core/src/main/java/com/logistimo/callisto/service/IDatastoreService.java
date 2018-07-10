package com.logistimo.callisto.service;

import com.logistimo.callisto.model.Datastore;

import java.util.List;

/**
 * Created by charan on 09/06/18.
 */
public interface IDatastoreService {

  Datastore get(String userId, String datastoreId);

  List<Datastore> getDatastoresByUser(String userId);

  void save(Datastore datastore);

  Datastore getDatastoreById(String datastoreId);
}
