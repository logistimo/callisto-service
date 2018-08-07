package com.logistimo.callisto.model;

import lombok.Data;

@Data
public class PageResultsModel {
  private Object result;
  private Long totalResultsCount;
}