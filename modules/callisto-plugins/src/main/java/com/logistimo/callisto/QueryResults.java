package com.logistimo.callisto;

import java.util.ArrayList;
import java.util.List;

/** @author Mohan Raja */
public class QueryResults {
  private List<String> headings;
  private List<CallistoDataType> dataTypes;
  private List<List<String>> rows;
  private List<String> rowHeadings;

  public List<String> getHeadings() {
    return headings;
  }

  public void setHeadings(List<String> headings) {
    this.headings = headings;
  }

  public List<CallistoDataType> getDataTypes() {
    return dataTypes;
  }

  public void setDataTypes(List<CallistoDataType> dataTypes) {
    this.dataTypes = dataTypes;
  }

  public void addRow(List<String> row) {
    if (rows == null) {
      rows = new ArrayList<>();
    }
    rows.add(row);
  }

  public List<List<String>> getRows() {
    return rows;
  }

  public List<String> getRowHeadings() {
    return rowHeadings;
  }

  public void setRowHeadings(List<String> rowHeadings) {
    this.rowHeadings = rowHeadings;
  }
}
