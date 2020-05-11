package com.logistimo.callisto.function;

import com.logistimo.callisto.AppConstants;
import com.logistimo.callisto.ICallistoFunction;
import com.logistimo.callisto.exception.CallistoException;
import java.math.BigDecimal;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component(value = "prev")
public class PreviousFunction implements ICallistoFunction {

  @Override
  public String getName() {
    return "prev";
  }

  private String[] getParameter(String value) {
    String val = value.trim();
    int fnStart = StringUtils.indexOf(val, AppConstants.OPEN_BRACKET);
    int fnEnd = StringUtils.lastIndexOf(val, AppConstants.CLOSE_BRACKET);
    String params = StringUtils.substring(val, fnStart + 1, fnEnd);
    return params.split(",");
  }

  @Override
  public String getResult(FunctionParam functionParam) throws CallistoException {
    String[] parameters = getParameter(functionParam.function);
    int columnIndex = functionParam.getResultHeadings().indexOf(parameters[0]);
    int sortByColumnIndex = functionParam.getResultHeadings().indexOf(parameters[1]);
    if (columnIndex < 0 || sortByColumnIndex < 0) {
      throw new CallistoException(
          "Column " + parameters[0] + " not found in results. Prev function failed");
    }
    List<String> resultRow = functionParam.getResultRow();
    String defaultResult = resultRow.get(columnIndex);
    if (StringUtils.isEmpty(resultRow.get(columnIndex)) || BigDecimal.ZERO
        .equals(new BigDecimal(resultRow.get(columnIndex)))) {
      return functionParam.getPreviousNonZeroValueOfColumnIfPresent(parameters[0], parameters[1],
          resultRow.get(sortByColumnIndex), functionParam.getDimensions()).orElse(defaultResult);
    }
    return defaultResult;
  }

  @Override
  public int getArgsLength() {
    return 2;
  }

  @Override
  public int getMinArgsLength() {
    return 2;
  }

  @Override
  public int getMaxArgLength() {
    return 2;
  }
}
