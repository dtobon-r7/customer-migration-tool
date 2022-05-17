package com.rapid7.intsightsmigrationtool.services.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UAP implements DTO {

    private String uapType;
    private String userId;
}
