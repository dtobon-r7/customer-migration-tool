package com.rapid7.intsightsmigrationtool.services.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * DTO to map the fields returned by the 'Create OrgProduct for Organization' API as described in the platform-public-api-app.
 * <a href="https://artifacts.bos.rapid7.com/nexus/content/sites/razor-site/platform-public-api-app/restdocs/api-guide.html#_create_orgproduct_for_organization"></a>
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Product implements DTO {

    private String productCode;
    private boolean productStatus;
}
