package com.logistimo.callisto;

/**
 * Created by chandrakant on 15/03/17.
 */
public enum CallistoDataType {
    NUMBER("number"),
    STRING("string");

    private String value;
    CallistoDataType(String s) {
        this.value = s;
    }
}
