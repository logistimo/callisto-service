package com.logistimo.callisto.Exception;

import com.logistimo.callisto.Resources;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author Mohan Raja
 */
public class CallistoException extends Exception {
  private String code;
  public CallistoException(String code, Locale locale, Object... arguments){
    super(constructMessage(code, locale, arguments));
    this.code = code;
  }

  public CallistoException(String message){
    super(message);
  }

  public CallistoException(String message, Throwable t){
    super(message, t);
  }

  public CallistoException(String code, Throwable t,  Object... arguments){
    super(constructMessage(code, Locale.ENGLISH, arguments),t);
    this.code = code;
  }

  public CallistoException(String code,  Object... arguments){
    super(constructMessage(code, Locale.ENGLISH, arguments));
    this.code = code;
  }

  public CallistoException(Throwable t) {
    super(t);
  }

  private static String constructMessage(String code, Locale locale, Object[] params) {
    if(locale == null){
      locale = Locale.ENGLISH;
    }
    ResourceBundle errors = Resources.get().getBundle("errors",locale);
    String message;
    try {
      message = errors.getString(code);
      if (params != null && params.length > 0) {
        return MessageFormat.format(message, params);
      }
    }catch (Exception ignored) {
      // ignored
    }
    return code;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }
}
