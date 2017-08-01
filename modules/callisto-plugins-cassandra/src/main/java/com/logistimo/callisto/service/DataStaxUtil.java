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

package com.logistimo.callisto.service;

import com.datastax.driver.core.DataType;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by chandrakant on 28/07/17.
 */
public class DataStaxUtil {
  public static Map<DataType.Name, Class> DATA_TYPE_MAPPING = new HashMap<>();
  static{
    DATA_TYPE_MAPPING.put(DataType.ascii().getName(), String.class);
    DATA_TYPE_MAPPING.put(DataType.decimal().getName(), BigDecimal.class);
    DATA_TYPE_MAPPING.put(DataType.text().getName(), String.class);
    DATA_TYPE_MAPPING.put(DataType.varchar().getName(), String.class);
    DATA_TYPE_MAPPING.put(DataType.smallint().getName(), Short.class);
    DATA_TYPE_MAPPING.put(DataType.tinyint().getName(), Byte.class);
    DATA_TYPE_MAPPING.put(DataType.bigint().getName(), Long.class);
    DATA_TYPE_MAPPING.put(DataType.cdouble().getName(), Double.class);
    DATA_TYPE_MAPPING.put(DataType.cint().getName(), Integer.class);
    DATA_TYPE_MAPPING.put(DataType.cfloat().getName(), Float.class);
  }
}
