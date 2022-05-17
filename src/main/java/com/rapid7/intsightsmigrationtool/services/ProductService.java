package com.rapid7.intsightsmigrationtool.services;

import com.rapid7.intsightsmigrationtool.services.dto.ProductRoles;
import com.rapid7.intsightsmigrationtool.services.dto.Role;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@Component
public class ProductService implements IdentityManagementService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ApiConfiguration apiConfiguration;

    public Map<String, Role> getSuggestedRolesForProduct(String productCode) {

        HttpHeaders headers = getRequestHeader(apiConfiguration, true);
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        Map<String, Role> roles = new HashMap<>();

        // Send API Request to create customer
        final String api = "rbac/v1/products/" + productCode + "/roles";
        ResponseEntity<?> response = restTemplate.exchange(apiConfiguration.getApiHost() + api, HttpMethod.GET, requestEntity, ProductRoles.class);

        if (response.getStatusCode().equals(HttpStatus.OK)) {
            ProductRoles responseRoles = (ProductRoles) response.getBody();
            if (responseRoles != null) {
                roles = responseRoles.getData().stream().collect(Collectors.toMap(Role::getRoleName, Function.identity()));
            }
        }

        return roles;
    }
}
