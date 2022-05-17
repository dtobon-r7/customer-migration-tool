package com.rapid7.intsightsmigrationtool.services.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrgProduct implements DTO {

    private Organization organization;
    private String productCode;
    private String productToken;
}
