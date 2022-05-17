package com.rapid7.intsightsmigrationtool.parser;

import lombok.Getter;

@Getter
public enum IntSightsInputKey {
  ACCOUNT_ID(0, "Account ID"),
  CUSTOMER_NAME(1, "Customer Name"),
  ORGANIZATION_NAME(2, "Organization Name"),
  ORGANIZATION_REGION(3, "Organization Region"),
  PRODUCT_CODE(4, "Product Code"),
  USER_EMAIL(5, "User Email"),
  USER_FIRST_NAME(6, "User First Name"),
  USER_LAST_NAME(7, "User Last Name"),
  USER_PLATFORM_ADMIN_STATUS(8, "User Platform Admin Status"),
  USER_PRODUCT_ROLES(9, "User Product Role(s)");

  IntSightsInputKey(Integer index, String columnName) {
    this.index = index;
    this.columnName = columnName;
  }

  private final String columnName;
  private final Integer index;
}
