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

package com.logistimo.callisto.function;

import com.logistimo.callisto.CharacterConstants;
import com.logistimo.callisto.ICallistoFunction;
import com.logistimo.callisto.exception.CallistoException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Function to convert DateTime from one format to another for display format. Requires 3 arguments
 * like: datetime(dateTobeParsed,dateFormat,requiredDateFormat) . DateFormat and requireDateFormat
 * should adhere to DateTime formats from {@link org.joda.time.format.DateTimeFormat}
 * See <a href="http://www.joda.org/joda-time/apidocs/org/joda/time/format/DateTimeFormat.html">joda</a>
 *
 * @author chandrakant
 */
@Component(value = "datetime")
public class DateTimeFunction implements ICallistoFunction {

  private static final Logger logger = Logger.getLogger(DateTimeFunction.class);
  private static String name = "datetime";
  private static Integer argsLength = 3;

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getResult(FunctionParam functionParam) throws CallistoException {
    String fn = functionParam.getFunction();
    List<String> params = getParam(fn);
    try {
      if (params != null && params.size() >= argsLength) {
        DateTime dateTime =
            DateTime.parse(params.get(0).trim(), DateTimeFormat.forPattern(params.get(1).trim()));
        return dateTime.toString(params.get(2));
      } else {
        logger.error("Error in datetime function parameters: " + fn);
      }
    } catch (IllegalArgumentException e) {
      logger.error("Joda: error in parsing datetime function arguments: " + fn, e);
      throw new CallistoException("Q103", fn);
    } catch (Exception e) {
      logger.error("Error in parsing datetime function getResult: " + fn, e);
    }
    return null;
  }

  public static List<String> getParam(String fn) throws CallistoException {
    String str = StringUtils.substring(fn, fn.indexOf(CharacterConstants.OPEN_BRACKET) + 1,
        fn.lastIndexOf(CharacterConstants.CLOSE_BRACKET));
    List<String> params = new ArrayList<>();
    String quote;
    int boundary = 0;
    try {
      for (int i = 0; i < argsLength; i++) {
        while (StringUtils.isEmpty(String.valueOf(str.charAt(boundary)).trim())) {
          boundary++;
        }
        if (Objects
            .equals(String.valueOf(str.charAt(boundary)), CharacterConstants.SINGLE_QUOTE)) {
          quote = CharacterConstants.SINGLE_QUOTE;
        } else {
          quote = null;
        }
        if (StringUtils.isNotEmpty(quote)) {
          int quoteEnd = str.indexOf(quote, boundary + 1);
          if (quoteEnd == -1) {
            throw new CallistoException("Q103", fn);
          }
          params.add(StringUtils.substring(str, boundary + 1, quoteEnd));
          boundary = quoteEnd + 2;
        } else {
          int nextComma = str.indexOf(CharacterConstants.COMMA, boundary);
          if (nextComma == -1) {
            nextComma = str.length();
          }
          params.add(StringUtils.substring(str, boundary, nextComma));
          boundary = nextComma + 1;
        }
      }
    } catch (StringIndexOutOfBoundsException ignored) {
      logger.error("Syntax error in datetime function: " + fn);
      throw new CallistoException("Q103", fn);
    } catch (Exception e) {
      logger.error("Error in parsing parameter of datetime function: " + fn, e);
    }
    return params;
  }

  @Override
  public int getArgsLength() {
    return argsLength;
  }

  @Override
  public int getMinArgsLength() {
    return -1;
  }

  @Override
  public int getMaxArgLength() {
    return -1;
  }
}
