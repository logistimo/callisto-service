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

import com.logistimo.callisto.model.QueryRequestModel;

import java.util.List;

/**
 * Wrapper class for holding different parameters of Functions
 * Created by chandrakant on 25/05/17.
 */
public class FunctionParam {
  private QueryRequestModel request;
  private List<String> resultHeadings;
  private List<String> resultRow;
  private List<String> rowHeadings; // row headings of QueryResult
  private String function;
  private boolean forceEnclose;
  private String escaping;

  public FunctionParam(){

  }

  public FunctionParam(QueryRequestModel request, String escaping, List<String> rowHeadings) {
    this.request = request;
    this.escaping = escaping;
    this.rowHeadings = rowHeadings;
  }

  public FunctionParam(
      QueryRequestModel request, List<String> headings, List<String> row, String function) {
    this.request = request;
    this.resultHeadings = headings;
    this.resultRow = row;
    this.function = function;
  }

  public QueryRequestModel getRequest() {
    return request;
  }

  public void setRequest(QueryRequestModel request) {
    this.request = request;
  }

  public List<String> getResultRow() {
    return resultRow;
  }

  public void setResultRow(List<String> resultRow) {
    this.resultRow = resultRow;
  }

  public String getFunction() {
    return function;
  }

  public void setFunction(String function) {
    this.function = function;
  }

  public List<String> getResultHeadings() {
    return resultHeadings;
  }

  public void setResultHeadings(List<String> resultHeadings) {
    this.resultHeadings = resultHeadings;
  }

  public QueryRequestModel getQueryRequestModel() {
    return request;
  }

  public void setQueryRequestModel(QueryRequestModel request) {
    this.request = request;
  }

  public boolean isForceEnclose() {
    return forceEnclose;
  }

  public void setForceEnclose(boolean forceEnclose) {
    this.forceEnclose = forceEnclose;
  }

  public String getEscaping() {
    return escaping;
  }

  public void setEscaping(String escaping) {
    this.escaping = escaping;
  }

  public List<String> getRowHeadings() {
    return rowHeadings;
  }

  public void setRowHeadings(List<String> rowHeadings) {
    this.rowHeadings = rowHeadings;
  }
}
