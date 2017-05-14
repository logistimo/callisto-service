package com.logistimo.callisto;

/**
 * @author Mohan Raja
 */
public enum FunctionType {
  CSV("csv"),
  ENCLOSE_CSV("enclosecsv");
  private String value;

  FunctionType(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return value;
  }

  public static FunctionType getFunctionType(String value) {
    for (FunctionType functionTypes : FunctionType.values()) {
      if(functionTypes.value.equals(value)) {
        return functionTypes;
      }
    }
    return null;
  }
}
