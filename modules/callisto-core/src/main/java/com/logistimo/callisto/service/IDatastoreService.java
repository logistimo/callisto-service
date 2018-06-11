package com.logistimo.callisto.service;

import com.logistimo.callisto.model.Datastore;

/**
 * Created by charan on 09/06/18.
 */
public interface IDatastoreService {

  Datastore get(String userId, String datastoreId);

  void save(String userId, Datastore datastore);

}
