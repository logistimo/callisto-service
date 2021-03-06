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

import com.logistimo.callisto.AppConstants;
import com.logistimo.callisto.ICallistoFunction;
import com.logistimo.callisto.exception.CallistoException;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

  private static final Logger logger = LoggerFactory.getLogger(DateTimeFunction.class);
  private static final String NAME = "datetime";
  private static final Integer ARGS_LENGTH = 3;

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public String getResult(FunctionParam functionParam) throws CallistoException {
    String fn = functionParam.function;
    List<String> params = getParameters(fn);
    try {
      if (params == null || params.size() < ARGS_LENGTH) {
        throw new CallistoException("Q103", fn);
      }
      DateTime dateTime =
          DateTime.parse(FunctionUtil.replaceVariables(params.get(0).trim(),
                  functionParam.getResultHeadings(), functionParam.getResultRow()),
              DateTimeFormat.forPattern(params.get(1).trim()));
      return dateTime.toString(params.get(2));
    } catch (IllegalArgumentException e) {
      logger.error("Joda: error in parsing datetime function arguments: " + fn, e);
      throw new CallistoException("Q103", fn);
    } catch (Exception e) {
      logger.error("Error in parsing datetime function getResult: " + fn, e);
    }
    return null;
  }


  public static List<String> getParameters(String fn) throws CallistoException {
    String str = StringUtils.substring(fn, fn.indexOf(AppConstants.OPEN_BRACKET) + 1,
        fn.lastIndexOf(AppConstants.CLOSE_BRACKET));
    List<String> params = new ArrayList<>();
    String quote;
    int boundary = 0;
    try {
      for (int i = 0; i < ARGS_LENGTH; i++) {
        while (StringUtils.isBlank(String.valueOf(str.charAt(boundary)))) {
          boundary++;
        }
        quote =
            Objects.equals(String.valueOf(str.charAt(boundary)), AppConstants.SINGLE_QUOTE)
                ? AppConstants.SINGLE_QUOTE : null;
        if (StringUtils.isNotEmpty(quote)) {
          int quoteEnd = str.indexOf(quote, boundary + 1);
          if (quoteEnd == -1) {
            throw new CallistoException("Q103", fn);
          }
          params.add(StringUtils.substring(str, boundary + 1, quoteEnd));
          boundary = str.indexOf(AppConstants.COMMA, quoteEnd) + 1;
        } else {
          int nextComma = str.indexOf(AppConstants.COMMA, boundary);
          if (nextComma == -1) {
            nextComma = str.length();
          }
          params.add(StringUtils.substring(str, boundary, nextComma).trim());
          boundary = nextComma + 1;
        }
      }
    } catch (Exception e) {
      logger.error("Exception while parsing parameters of datetime function: " + fn, e);
      throw new CallistoException("Q103", fn);
    }
    return params;
  }

  @Override
  public int getArgsLength() {
    return ARGS_LENGTH;
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
