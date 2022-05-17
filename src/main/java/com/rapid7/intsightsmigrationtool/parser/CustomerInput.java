package com.rapid7.intsightsmigrationtool.parser;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class CustomerInput {

  private String accountID;
  private String customerName;
  private String organizationName;
  private String organizationRegion;
  private String productCode;
  private Map<String, UserInput> users;
}
