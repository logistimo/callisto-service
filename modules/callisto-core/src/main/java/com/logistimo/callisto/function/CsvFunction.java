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

import com.logistimo.callisto.CallistoDataType;
import com.logistimo.callisto.CharacterConstants;
import com.logistimo.callisto.ICallistoFunction;
import com.logistimo.callisto.QueryResults;
import com.logistimo.callisto.exception.CallistoException;
import com.logistimo.callisto.model.QueryRequestModel;
import com.logistimo.callisto.service.IQueryService;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;

import javax.annotation.Resource;

/**
 * Created by chandrakant on 18/05/17.
 */
@Component(value = "csv")
public class CsvFunction implements ICallistoFunction {

  private static final String NAME = "csv";
  @Resource IQueryService queryService;

  public String getCSV(FunctionParam param) throws CallistoException {
    return getCSV(param, false);
  }

  /**
   *
   * @param param FunctionParam wrapper to contain function, queryRequstModel and query escaping String
   * @param forceEnclose true if results have to be enclosed with single quotes
   * @return a csv of results returned by the query
   * @throws CallistoException
   */
  public String getCSV(FunctionParam param, boolean forceEnclose)
      throws CallistoException {
    StringBuilder csv = new StringBuilder();
    QueryFunction
        function =
        QueryFunction.getQueryFunction(param.function, param.getQueryRequestModel().filters);
    QueryResults results =
        queryService.readData(buildQueryRequestModel(param, function));
    for (List<String> strings : results.getRows()) {
      if (!forceEnclose
          && results.getDataTypes() != null
          && CallistoDataType.NUMBER.equals(results.getDataTypes().get(0))) {
        csv.append(strings.get(0)).append(CharacterConstants.COMMA);
      } else {
        String enclosing;
        if (strings.get(0).contains(CharacterConstants.SINGLE_QUOTE)
            && StringUtils.isNotEmpty(param.getEscaping())) {
          enclosing = param.getEscaping();
        }else{
          enclosing = CharacterConstants.SINGLE_QUOTE;
        }
        csv.append(enclosing)
            .append(strings.get(0))
            .append(enclosing)
            .append(CharacterConstants.COMMA);
      }
    }
    if (function.fill) {
      for (List<String> rows : results.getRows()) {
        if (StringUtils.isNotEmpty(rows.get(0))) {
          param.getRowHeadings().add(rows.get(0));
        }
      }
    }
    csv.setLength(csv.length() - 1);
    return csv.toString();
  }

  private QueryRequestModel buildQueryRequestModel(FunctionParam functionParam,
                                                   QueryFunction function) {
    QueryRequestModel nestedQueryResultModel = new QueryRequestModel();
    nestedQueryResultModel.userId = functionParam.getQueryRequestModel().userId;
    nestedQueryResultModel.queryId = function.queryID;
    nestedQueryResultModel.filters = functionParam.getQueryRequestModel().filters;
    nestedQueryResultModel.offset = function.offset;
    nestedQueryResultModel.size = function.size;
    return nestedQueryResultModel;
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public String getResult(FunctionParam functionParam) throws CallistoException {
    return getCSV(functionParam, false);
  }

  @Override
  public int getArgsLength() {
    return -1;
  }

  @Override
  public int getMinArgsLength() {
    return 1;
  }

  @Override
  public int getMaxArgLength() {
    return 4;
  }
}
