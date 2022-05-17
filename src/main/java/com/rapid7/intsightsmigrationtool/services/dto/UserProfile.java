package com.rapid7.intsightsmigrationtool.services.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserProfile implements DTO {
    private String firstName;
    private String lastName;
    private String email;
    private String userId;
}
