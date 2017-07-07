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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * @author Mohan Raja
 */
public class Resources {

  private static final Resources SINGLETON = new Resources();
  private final Map<String,ResourceBundle> rmap = new HashMap<>();

  public static Resources get() { return SINGLETON; }

  public ResourceBundle getBundle( String baseName, Locale locale ) throws
      MissingResourceException {
    if ( baseName == null || locale == null ) {
      return null;
    }
    String key = baseName + "_" + locale.toString();
    ResourceBundle bundle = rmap.get( key );
    if ( bundle == null ) {
      key = baseName + "_" + locale.getLanguage();
      bundle = rmap.get( key );
      if ( bundle == null ) {
        bundle = getUTF8Bundle( baseName, locale );
        key = baseName + "_" + bundle.getLocale().toString();
        rmap.put( key, bundle );
      }
    }
    return bundle;
  }

  public void destroy() {
    rmap.clear();
  }

  private static ResourceBundle getUTF8Bundle( String baseName, Locale locale ) {
    return ResourceBundle.getBundle( baseName, locale );
  }
}