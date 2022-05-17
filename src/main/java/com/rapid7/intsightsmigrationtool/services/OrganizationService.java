package com.rapid7.intsightsmigrationtool.services;

import com.rapid7.intsightsmigrationtool.services.dto.OrgProduct;
import com.rapid7.intsightsmigrationtool.services.state.State;
import lombok.AllArgsConstructor;
import org.json.JSONObject;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Component
public class OrganizationService implements IdentityManagementService {

    private RestTemplate restTemplate;

    private ApiConfiguration apiConfiguration;

    private State state;


    /////////////////////////////
    //     PUBLIC  METHODS     //
    /////////////////////////////

    /**
     * Create an OrgProduct record for the organization with the given Identifier using the following API
     * <a href="https://artifacts.bos.rapid7.com/nexus/content/sites/razor-site/platform-public-api-app/restdocs/api-guide.html#_create_orgproduct_for_organization">Create OrgProduct for Organization</a>
     *
     * @param organizationId the organization unique identifier
     * @param productCode    the product code identifier (e.g. TC)
     * @param productStatus  the product status description
     * @return the result of creating a new OrgProduct
     * @throws RetryableException if the API calls fail more than the maxAttempts
     */
    @Retryable(value = {RetryableException.class}, maxAttempts = 4, backoff = @Backoff(delay = 15000))
    public Result<OrgProduct> createOrgProductForOrganization(String organizationId, String productCode, String productStatus) throws RetryableException {

        Result<OrgProduct> result = new Result<>();

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

    /**
     * Add a user with the given email to the organization with the given identifier using the following API.
     * <a href="https://artifacts.bos.rapid7.com/nexus/content/sites/razor-site/platform-public-api-app/restdocs/api-guide.html#_add_user_to_orgproduct">Add User to OrgProduct</a>
     *
     * @param organizationId the organization unique identifier
     * @param productToken   the OrgProduct's token
     * @param userEmail      The user's email to add to the Organization
     * @param admin          whether the user is a Platform admin
     * @return the result of adding the user to the OrgProduct
     * @throws RetryableException if the API calls fail more than the maxAttempts
     */
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

    /////////////////////////////
    // PRIVATE HELPER METHODS //
    /////////////////////////////

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
