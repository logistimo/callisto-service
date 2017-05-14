package com.logistimo.callisto;

import java.util.ArrayList;
import java.util.List;

/** @author Mohan Raja */
public class DataSourceType {
  public String name;
  public List<String> metaFields;

  public void addMeta(String metaField) {
    if (metaFields == null) {
      metaFields = new ArrayList<>();
    }
    metaFields.add(metaField);
  }
}
