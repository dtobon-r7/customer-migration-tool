package com.rapid7.intsightsmigrationtool.services;

import com.rapid7.intsightsmigrationtool.services.dto.OrgProduct;
import com.rapid7.intsightsmigrationtool.services.state.State;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@NoArgsConstructor
@Component
public class OrganizationService implements IdentityManagementService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ApiConfiguration apiConfiguration;

    @Autowired
    private State state;


    @Retryable(value = {RetryableException.class}, maxAttempts = 4, backoff = @Backoff(delay = 15000))
    public Result<OrgProduct> createOrgProductForOrganization(String organizationId, String productCode, String productStatus) throws RetryableException {

        Result<OrgProduct> result = new Result<OrgProduct>();

        if (state.hasOrgProductBeenCreated(organizationId)) {
            result.setMessage("Organization: " + organizationId + " had been previously created.");
        }

        List<OrgProduct> orgProducts = getOrgProductsForOrganization(organizationId);
        Optional<OrgProduct> optional = orgProducts.stream().filter(org -> org.getOrganization().getOrganizationId().equals(organizationId)).findFirst();

        if (optional.isPresent()) {
            result.setEntity(optional.get());
            return result;
        }

        // Create payload for request
        HttpHeaders headers = getRequestHeader(apiConfiguration, false);
        JSONObject request = new JSONObject();
        request.put("productCode", productCode);
        request.put("productStatus", productStatus);
        //TODO Verify this value
        request.put("managed", true);
        HttpEntity<String> requestEntity = new HttpEntity<>(request.toString(), headers);

        // Send API Request to create Organization for Customer
        final String api = "api/1/organizations/" + organizationId + "/product";
        ResponseEntity<?> response = sendRequest(restTemplate, apiConfiguration.getApiHost() + api, HttpMethod.POST, requestEntity, OrgProduct.class);

        if (response.getStatusCode().equals(HttpStatus.CREATED)) {
            OrgProduct orgProduct = (OrgProduct) response.getBody();
            if (orgProduct != null) {
                result = new Result<>(true, "Successfully Created Product Token: " + orgProduct.getProductToken() + " for organization: " + orgProduct.getOrganization().getOrganizationId(), (OrgProduct) response.getBody());
                updateOrgProductState(organizationId, orgProduct.getProductToken());
            }
        }

        return result;
    }


    @Retryable(value = {RetryableException.class}, maxAttempts = 4, backoff = @Backoff(delay = 15000))
    public Result<?> addUserToOrgProduct(String organizationId, String productToken, String userEmail, boolean admin) throws RetryableException {

        Result<?> result = new Result();

        // Create payload for request
        HttpHeaders headers = getRequestHeader(apiConfiguration, false);
        JSONObject request = new JSONObject();
        request.put("email", userEmail);
        request.put("admin", admin);
        HttpEntity<String> requestEntity = new HttpEntity<>(request.toString(), headers);

        // Send API Request to create Organization for Customer
        final String api = "api/1/organizations/" + organizationId + "/product/" + productToken + "/user";
        ResponseEntity<?> response = sendRequest(restTemplate, apiConfiguration.getApiHost() + api, HttpMethod.POST, requestEntity, Object.class);

        if (response.getStatusCode().equals(HttpStatus.OK)) {
            result = new Result<>(true, "Successfully added user: " + userEmail + " to OrgProduct", null);
        }

        return result;
    }

    private List<OrgProduct> getOrgProductsForOrganization(String organizationId) {
        // Create payload for request
        HttpHeaders headers = getRequestHeader(apiConfiguration, false);
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        List<OrgProduct> orgProducts = null;

        // Send API Request to create customer
        final String api = "api/1/organizations/" + organizationId;
        ResponseEntity<?> response = restTemplate.exchange(apiConfiguration.getApiHost() + api, HttpMethod.GET, requestEntity, OrgProduct[].class);

        if (response.getStatusCode().equals(HttpStatus.OK)) {
            orgProducts = Arrays.asList((OrgProduct[]) response.getBody());
        }

        return orgProducts;
    }

    public void updateOrgProductState(String organizationId, String productToken) {
        state.updateOrgProductState(organizationId + "," + productToken);
        state.addOrgProduct(organizationId, productToken);
    }

}
