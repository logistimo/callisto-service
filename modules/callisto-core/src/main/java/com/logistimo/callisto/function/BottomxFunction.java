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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.logistimo.callisto.CharacterConstants;
import com.logistimo.callisto.ICallistoFunction;
import com.logistimo.callisto.exception.CallistoException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by chandrakant on 04/08/17.
 * BottomxFunction defines a function to modify a map to return bottom (compared by value) x entries
 * Accpets 3 parameters, variable name (type = map), size of the resulting map and offset
 */
@Component(value = "bottomx")
public class BottomxFunction implements ICallistoFunction {

  private static final String NAME = "bottomx";

  private List<String> getParameters(String fn) {
    String str = StringUtils.substring(fn, fn.indexOf(CharacterConstants.OPEN_BRACKET) + 1,
        fn.lastIndexOf(CharacterConstants.CLOSE_BRACKET));
    String[] arr = StringUtils.split(str, CharacterConstants.COMMA);
    return Arrays.asList(StringUtils.join(arr, CharacterConstants.COMMA, 0, arr.length - 2),
        arr[arr.length - 2], arr[arr.length - 1]);
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public String getResult(FunctionParam functionParam) throws CallistoException {
    List<String> params = getParameters(functionParam.function);
    String mapStr = FunctionUtil.replaceVariables(params.get(0).trim(),
        functionParam.getResultHeadings(), functionParam.getResultRow());
    Map<String, Long> map;
    Integer size;
    Integer offset;
    try {
      if(StringUtils.isNotEmpty(mapStr)){
        map = new Gson().fromJson(mapStr, new TypeToken<Map<String, Long>>() {
        }.getType());
      } else{
        map = new LinkedHashMap<>();
      }
    } catch (Exception e) {
      throw new CallistoException("Q104", mapStr);
    }
    try {
      size =
          Integer.valueOf(functionParam.getQueryRequestModel().filters.get(params.get(1).trim()));
      offset =
          Integer.valueOf(functionParam.getQueryRequestModel().filters.get(params.get(2).trim()));
    } catch (NumberFormatException e) {
      throw new CallistoException("Q105", params.get(1) + CharacterConstants.COMMA + params.get(2));
    }
    Map sortedMap = map.entrySet().stream()
        .sorted(this::compareEntry)
        .skip(offset)
        .limit(size)
        .collect(Collectors
            .toMap(Entry::getKey, Entry::getValue, (v1, v2) -> v1, LinkedHashMap::new));
    return new Gson().toJson(sortedMap);
  }

  private int compareEntry(Map.Entry<String, Long> e1, Map.Entry<String, Long> e2) {
    if(Objects.equals(e1.getValue(), e2.getValue())){
      return e2.getKey().compareTo(e1.getKey());
    }else{
      return Long.compare(e1.getValue(),e2.getValue());
    }
  }

  @Override
  public int getArgsLength() {
    return 3;
  }

  @Override
  public int getMinArgsLength() {
    return 3;
  }

  @Override
  public int getMaxArgLength() {
    return 3;
  }
}
