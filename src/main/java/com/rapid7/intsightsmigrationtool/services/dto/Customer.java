package com.rapid7.intsightsmigrationtool.services.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * DTO to map the fields returned by the 'Create a Customer' API as described in the platform-public-api-app.
 * <a href="https://artifacts.bos.rapid7.com/nexus/content/sites/razor-site/platform-public-api-app/restdocs/api-guide.html#_create_a_customer">Create a Customer</a>
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Customer implements DTO {

  private String customerId;
  private String name;
  private String oktaGroupId;
  private String oktaAdminGroupId;
  private String oktaMfaExclusionGroupId;
}
