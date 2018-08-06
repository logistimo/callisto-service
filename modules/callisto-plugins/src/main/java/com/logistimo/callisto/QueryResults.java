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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    if(rowHeadings != null && !rowHeadings.isEmpty()){
      this.rowHeadings = rowHeadings;
    }
  }

  /**
   *
   * @param rowHeadings
   * @param index index of rowHeading element
   * @return QueryResults after filling dummy rows for the all absent rowHeading elements
   */
  public void fillResults(List<String> rowHeadings, Integer index) {
    if (rowHeadings != null && !rowHeadings.isEmpty()) {
      Set<String> rowHeadingsSet = new HashSet<>(rowHeadings);
      if (getRows() != null) {
        for (List row : getRows()) {
          rowHeadingsSet.remove(row.get(index));
        }
      }
      for (String heading : rowHeadings) {
        String[] nRow = new String[index + 1];
        Arrays.fill(nRow, "");
        nRow[index] = heading;
        addRow(Arrays.asList(nRow));
      }
    }
  }
}
