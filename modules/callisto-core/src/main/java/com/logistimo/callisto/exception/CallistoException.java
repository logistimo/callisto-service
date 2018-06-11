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

package com.logistimo.callisto.exception;

import com.logistimo.callisto.Resources;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author Mohan Raja
 */
public class CallistoException extends RuntimeException {

  public static final String RESULT_FAILURE = "failure";

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
