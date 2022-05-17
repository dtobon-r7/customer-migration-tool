package com.rapid7.intsightsmigrationtool.services.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Organization implements DTO {

  private String organizationId;
  private String organizationName;
  private String regionCode;
  private Customer customer;
}
