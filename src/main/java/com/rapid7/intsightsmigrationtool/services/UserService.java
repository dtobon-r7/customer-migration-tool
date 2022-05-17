package com.rapid7.intsightsmigrationtool.services;

import com.rapid7.intsightsmigrationtool.services.dto.UAP;
import com.rapid7.intsightsmigrationtool.services.dto.UserProfile;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@AllArgsConstructor
@NoArgsConstructor
@Component
public class UserService implements IdentityManagementService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ApiConfiguration apiConfiguration;


    public boolean testPublicApiKey() {
        HttpHeaders headers = getRequestHeader(apiConfiguration, false);
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        // Send API Request to create customer
        final String api = "api/1/users/_search?email=xteam-test@rapid7.com";
        ResponseEntity<Object> response = restTemplate.exchange(apiConfiguration.getApiHost() + api, HttpMethod.GET, requestEntity, Object.class);

        if (response.getStatusCode().equals(HttpStatus.OK) || response.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
            return true;
        }

        return false;
    }

    public boolean testRBACKey() {
        HttpHeaders headers = getRequestHeader(apiConfiguration, true);
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        // Send API Request to create customer
        final String api = "rbac/v1/me/uap";
        ResponseEntity<UAP> response = restTemplate.exchange(apiConfiguration.getApiHost() + api, HttpMethod.GET, requestEntity, UAP.class);

        if (response.getStatusCode().equals(HttpStatus.OK)) {
            return true;
        }

        return false;
    }

    public UserProfile getUserProfile(String email) {
        HttpHeaders headers = getRequestHeader(apiConfiguration, false);
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        // Send API Request to create customer
        final String api = "api/1/users/_search?email=" + email;
        ResponseEntity<?> response = restTemplate.exchange(apiConfiguration.getApiHost() + api, HttpMethod.GET, requestEntity, UserProfile.class);

        if (response.getStatusCode().equals(HttpStatus.OK)) {
            return (UserProfile) response.getBody();
        }

        return null;
    }

}
