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