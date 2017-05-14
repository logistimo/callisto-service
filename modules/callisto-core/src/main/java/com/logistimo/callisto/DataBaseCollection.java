package com.logistimo.callisto;

import com.logistimo.callisto.service.IDataBaseService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/** @author Mohan Raja */
@Service
public class DataBaseCollection {

  private Map<String, IDataBaseService> databaseServices;

  private Map<String, DataSourceType> metaFields;

  DataBaseCollection(Map<String, IDataBaseService> databaseServices) {
    this.databaseServices = databaseServices;
    registerAllPluginMetafields();
  }

  public IDataBaseService getDataBaseService(String serviceName) {
    if (databaseServices.containsKey(serviceName)) {
      return databaseServices.get(serviceName);
    }
    throw new UnsupportedOperationException("unsupported database");
  }

  public void registerAllPluginMetafields() {
    metaFields = new HashMap<>(databaseServices.size());
    for (String dataservice : databaseServices.keySet()) {
      metaFields.put(dataservice, databaseServices.get(dataservice).getMetaFields());
    }
  }
}
