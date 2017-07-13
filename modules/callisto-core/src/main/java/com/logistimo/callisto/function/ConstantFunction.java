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
import com.logistimo.callisto.exception.CallistoException;
import com.logistimo.callisto.ICallistoFunction;
import com.logistimo.callisto.service.IConstantService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Created by chandrakant on 18/05/17. */
@Component(value = "constant")
public class ConstantFunction implements ICallistoFunction {

  private static String name = "constant";
  private static Integer argsLength = 1;
  private static Integer minArgsLength = argsLength;
  private static Integer maxArgsLength = argsLength;

  @Autowired IConstantService constantService;

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getResult(FunctionParam functionParam) throws CallistoException {
    String param = getParameter(functionParam.getFunction());
    return constantService.readConstant(functionParam.getRequest().userId, param).getConstant();
  }

  @Override
  public int getArgsLength() {
    return argsLength;
  }

  @Override
  public int getMinArgsLength() {
    return minArgsLength;
  }

  @Override
  public int getMaxArgLength() {
    return maxArgsLength;
  }

  public static String getParameter(String value) {
    String val = value.trim();
    int fnStart = val.indexOf(CharacterConstants.OPEN_BRACKET);
    int fnEnd = val.indexOf(CharacterConstants.CLOSE_BRACKET);
    return StringUtils.split(
            StringUtils.substring(val, fnStart + 1, fnEnd), CharacterConstants.COMMA)[
        0];
  }
}
