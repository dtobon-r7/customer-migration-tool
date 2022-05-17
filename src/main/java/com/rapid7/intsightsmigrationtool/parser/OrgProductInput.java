package com.rapid7.intsightsmigrationtool.parser;

import lombok.Data;

@Data
public class OrgProductInput {

    private String productCode;
    private String productStatus;
    private boolean managed;
    private Integer expirationTime;
}
