/*
 * Copyright © 2017 Logistimo.
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
    for (Map.Entry<String, IDataBaseService> entry : databaseServices.entrySet()) {
      metaFields.put(entry.getKey(), entry.getValue().getMetaFields());
    }
  }
}
