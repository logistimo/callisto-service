package com.logistimo.callisto.exception;

public class DuplicateQueryIdException extends RuntimeException {
  public DuplicateQueryIdException(String msg, Exception e) {
    super(msg, e);
  }
  public DuplicateQueryIdException(String msg) {
    super(msg);
  }
}