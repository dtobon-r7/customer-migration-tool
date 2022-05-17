package com.rapid7.intsightsmigrationtool.parser;

import lombok.Data;

@Data
public class ProductInput {

  private String productCode;
  private boolean productStatus;
  private String netSuiteId;
  private boolean managed;
  private int expirationTime;
}
