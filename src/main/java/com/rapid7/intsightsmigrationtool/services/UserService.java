package com.rapid7.intsightsmigrationtool.services;

import com.rapid7.intsightsmigrationtool.services.dto.UAP;
import com.rapid7.intsightsmigrationtool.services.dto.UserProfile;
import lombok.AllArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@AllArgsConstructor
@Component
public class UserService implements IdentityManagementService {

    private RestTemplate restTemplate;

    private ApiConfiguration apiConfiguration;


    /**
     * Test to ensure the configured Public API key is valid
     *
     * @return whether the key is valid
     */
    public boolean testPublicApiKey() {
        HttpHeaders headers = getRequestHeader(apiConfiguration, false);
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        // Send API Request to create customer
        final String api = "api/1/users/_search?email=xteam-test@rapid7.com";
        ResponseEntity<Object> response = restTemplate.exchange(apiConfiguration.getApiHost() + api, HttpMethod.GET, requestEntity, Object.class);

        return response.getStatusCode().equals(HttpStatus.OK) || response.getStatusCode().equals(HttpStatus.NOT_FOUND);
    }

    /**
     * Test to ensure the configured Public API key is valid
     *
     * @return whether the key is valid
     */
    public boolean testRBACKey() {
        HttpHeaders headers = getRequestHeader(apiConfiguration, true);
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        // Send API Request to create customer
        final String api = "rbac/v1/me/uap";
        ResponseEntity<UAP> response = restTemplate.exchange(apiConfiguration.getApiHost() + api, HttpMethod.GET, requestEntity, UAP.class);

        return response.getStatusCode().equals(HttpStatus.OK);
    }

    /**
     * Retrieves the User Profile for the given email identifier only if that user belongs to the Customer with the given identifier.
     *
     * @param customerId the customer the user belongs to
     * @param email      the unique email address from the User account
     * @return the UserProfile for the given email identifier.
     */
    public UserProfile getUserProfile(String customerId, String email) {
        HttpHeaders headers = getRequestHeader(apiConfiguration, false);
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        // Send API Request to create customer
        final String api = "api/1/users/_search?email=" + email;
        ResponseEntity<?> response = restTemplate.exchange(apiConfiguration.getApiHost() + api, HttpMethod.GET, requestEntity, UserProfile.class);

        if (response.getStatusCode().equals(HttpStatus.OK)) {
            UserProfile userProfile = (UserProfile) response.getBody();
            HttpHeaders rbacHeaders = getRequestHeader(apiConfiguration, true);
            HttpEntity<String> rbacRequestEntity = new HttpEntity<>(rbacHeaders);
            final String rbacApi = "rbac/v1/customers/" + customerId + "/users/" + userProfile.getUserId();

            try {
                ResponseEntity<?> rbacResponse = restTemplate.exchange(apiConfiguration.getApiHost() + rbacApi, HttpMethod.GET, rbacRequestEntity, Object.class);

                if (rbacResponse.getStatusCode().equals(HttpStatus.OK)) {
                    return (UserProfile) response.getBody();
                }
            } catch (HttpClientErrorException e) {
                if (e.getRawStatusCode() == HttpStatus.NOT_FOUND.value()) {
                    return null;
                }
            }
        }

        return null;
    }
}
