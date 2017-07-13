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

package com.logistimo.callisto;

import com.logistimo.callisto.exception.CallistoException;
import com.logistimo.callisto.function.FunctionParam;
import com.logistimo.callisto.function.FunctionsUtil;
import com.logistimo.callisto.model.QueryRequestModel;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chandrakant on 19/05/17.
 */
@Component
public class ResultManager {

  private static final Logger logger = Logger.getLogger(ResultManager.class);

  @Autowired private FunctionManager functionManager;

  public QueryResults getDesiredResult(
      QueryRequestModel request,
      QueryResults results,
      Map<String, String> desiredResultFormat)
      throws CallistoException {
    List<String> headings = results.getHeadings();
    QueryResults dResult = new QueryResults();
    dResult.setHeadings(new ArrayList<>(desiredResultFormat.keySet()));
    if (results.getHeadings() != null && results.getRows() != null) {
      for (List row : results.getRows()) {
        List<String> dRow = new ArrayList<>(dResult.getHeadings().size());
        for (Map.Entry<String, String> entry : desiredResultFormat.entrySet()) {
          String r =
              parseDesiredValue(request,
                  entry.getValue()
                      .replaceAll("\\s+", "")
                      .replaceAll("\n", "")
                      .replaceAll("\t", ""),
                  headings,
                  row);
          dRow.add(r);
        }
        dResult.addRow(dRow);
      }
    }
    return dResult;
  }

  private String parseDesiredValue(
      QueryRequestModel request, String str, List<String> headings, List<String> row)
      throws CallistoException {
    int index;
    List<String> fnVars = FunctionsUtil.getAllFunctionsVariables(str);
    for (int i = 0; i < fnVars.size(); i++) {
      if ((index = variableIndex(fnVars.get(i), headings)) > -1) {
        str = str.replace(fnVars.get(i), row.get(index));
      } else if (FunctionsUtil.isFunction(fnVars.get(i), false)) {
        String functionType = FunctionsUtil.getFunctionType(fnVars.get(i));
        if (functionType != null) {
          ICallistoFunction function = functionManager.getFunction(functionType);
          if (function == null) {
            throw new CallistoException("Q001", fnVars.get(i));
          }
          FunctionParam functionParam = new FunctionParam(request, headings, row, fnVars.get(i));
          str = str.replace(fnVars.get(i), function.getResult(functionParam));
        } else {
          throw new CallistoException("Q001", fnVars.get(i));
        }
      }
    }
    str = StringUtils.replace(str, CharacterConstants.ADD, CharacterConstants.EMPTY);
    str = StringUtils.replace(str, CharacterConstants.DOUBLE_QUOTE, CharacterConstants.EMPTY);
    str = StringUtils.replace(str, CharacterConstants.SINGLE_QUOTE, CharacterConstants.EMPTY);
    return str;
  }

  /**
   * @param val      desired value to be parsed
   * @param headings original result headings
   * @return checks if val is only a variable i.e. $xyz, and return index of variable in heading,
   * otherwise -1
   */
  public static int variableIndex(String val, List<String> headings) {
    int index = -1;
    if (headings != null
        && val.startsWith(CharacterConstants.SINGLE_DOLLAR)
        && !val.contains(CharacterConstants.FN_ENCLOSE)) {
      index = headings.indexOf(StringUtils.substring(val, 1));
      if (index == -1) {
        logger.error("Variable " + val + " not found in heading of QueryResult");
      }
    }
    return index;
  }
}
