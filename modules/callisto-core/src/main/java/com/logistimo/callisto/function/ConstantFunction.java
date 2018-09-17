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
import com.logistimo.callisto.model.ConstantText;
import com.logistimo.callisto.service.IConstantService;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Created by chandrakant on 18/05/17. */
@Component(value = "constant")
public class ConstantFunction implements ICallistoFunction {

  private static final String NAME = "constant";
  private static final Integer ARGS_LENGTH = 1;
  private static final Integer MIN_ARGS_LENGTH = 1;
  private static final Integer MAX_ARGS_LENGTH = 1;

  private IConstantService constantService;

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public String getResult(FunctionParam functionParam) throws CallistoException {
    String param = getParameter(functionParam.function);
    ConstantText constantText = constantService.readConstant(functionParam.getRequest().userId,
        param);
    if(constantText == null) {
      throw new CallistoException("Q106", functionParam.function);
    }
    return constantText.getConstant();
  }

  @Override
  public int getArgsLength() {
    return ARGS_LENGTH;
  }

  @Override
  public int getMinArgsLength() {
    return MIN_ARGS_LENGTH;
  }

  @Override
  public int getMaxArgLength() {
    return MAX_ARGS_LENGTH;
  }

  public static String getParameter(String value) {
    String val = value.trim();
    int fnStart = val.indexOf(CharacterConstants.OPEN_BRACKET);
    int fnEnd = val.indexOf(CharacterConstants.CLOSE_BRACKET);
    return StringUtils.split(
            StringUtils.substring(val, fnStart + 1, fnEnd), CharacterConstants.COMMA)[
        0];
  }

  @Autowired
  public void setConstantService(IConstantService constantService) {
    this.constantService = constantService;
  }
}
