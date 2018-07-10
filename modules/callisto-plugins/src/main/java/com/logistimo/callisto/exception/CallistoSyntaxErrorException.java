package com.logistimo.callisto.exception;

public class CallistoSyntaxErrorException extends RuntimeException {
  public CallistoSyntaxErrorException(String msg, Exception e) {
    super(msg, e);
  }
}