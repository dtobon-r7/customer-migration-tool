package com.rapid7.intsightsmigrationtool.services;

import com.rapid7.intsightsmigrationtool.parser.UserInput;
import com.rapid7.intsightsmigrationtool.services.dto.*;
import com.rapid7.intsightsmigrationtool.services.state.State;
import lombok.AllArgsConstructor;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@AllArgsConstructor
@Component
public class CustomerService implements IdentityManagementService {

    private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);

    private RestTemplate restTemplate;

    private UserService userService;

    private ApiConfiguration apiConfiguration;

    private State state;

    /////////////////////////////
    //     PUBLIC  METHODS     //
    /////////////////////////////

    /**
     * Creates a customer record using the following API
     * <a href="https://artifacts.bos.rapid7.com/nexus/content/sites/razor-site/platform-public-api-app/restdocs/api-guide.html#_create_a_customer">Create a Customer</a>
     *
     * @param customerName The name to use when creating the customer
     * @return Result of creating the new user.
     * @throws RetryableException
     */
    @Retryable(value = {RetryableException.class}, maxAttempts = 4, backoff = @Backoff(delay = 15000))
    public Result<Customer> createCustomer(String customerName) throws RetryableException {

        Result<Customer> result = new Result<Customer>();

        if (state.hasCustomerBeenMigrated(customerName)) {
            Customer migratedCustomer = getCustomerById(state.getCustomerId(customerName));
            logger.warn("Customer with the given name: " + customerName + " has been previously migrated with this tool ");
            return new Result<>(false, "Customer: " + customerName + " has been previously migrated. " +
                    "Please remove line from state file if you wish to recreate customer account.", migratedCustomer);
        }

        // Create payload for request
        HttpHeaders headers = getRequestHeader(apiConfiguration, false);
        JSONObject request = new JSONObject();
        request.put("name", customerName);
        HttpEntity<String> requestEntity = new HttpEntity<>(request.toString(), headers);

        // Send API Request to create customer
        final String api = "api/1/customers";
        ResponseEntity<?> response = sendRequest(restTemplate, apiConfiguration.getApiHost() + api, HttpMethod.POST, requestEntity, Customer.class);

        if (response.getStatusCode().equals(HttpStatus.CREATED)) {
            Customer customer = (Customer) response.getBody();
            if (customer != null) {
                result = new Result<>(true, "Successfully Created Customer: " + customer.getName() + " and CustomerId: " +
                        customer.getCustomerId(), (Customer) response.getBody());
                updateCustomerState(customerName, result.getEntity().getCustomerId());
            }
        }

        return result;
    }

    /**
     * Creates a new Organization for Customer using the following API
     * <a href="https://artifacts.bos.rapid7.com/nexus/content/sites/razor-site/platform-public-api-app/restdocs/api-guide.html#_create_a_new_organization_for_customer">Create a new Organization for Customer</a>
     *
     * @param customer         the customer record to create the new Organization for
     * @param organizationName the name to user for the Organization
     * @param regionCode       the Organization Region Code (e.g. us, eu...)
     * @return the result of creating a new Organization for the Customer
     * @throws RetryableException
     */
    @Retryable(value = {RetryableException.class}, maxAttempts = 4, backoff = @Backoff(delay = 15000))
    public Result<Organization> createOrgForCustomer(Customer customer, String organizationName, String regionCode) throws RetryableException {

        Result<Organization> result = new Result<Organization>();
        String customerId = customer.getCustomerId();

        if (state.hasOrganizationForCustomerBeenCreated(customerId)) {
            result.setMessage("Organization: " + organizationName + " has been previously created.");
        }

        List<Organization> organizations = getOrganizationsForCustomer(customerId);
        Optional<Organization> optional = organizations.stream().filter(org -> org.getOrganizationName().equals(organizationName)).findFirst();

        if (optional.isPresent()) {
            result.setEntity(optional.get());
            return result;
        }

        // Create payload for request
        HttpHeaders headers = getRequestHeader(apiConfiguration, false);
        JSONObject request = new JSONObject();
        request.put("organizationName", organizationName);
        request.put("regionCode", regionCode);
        request.put("organizationDisplayName", organizationName);
        HttpEntity<String> requestEntity = new HttpEntity<>(request.toString(), headers);

        // Send API Request to create Organization for Customer
        final String api = "api/1/customers/" + customer.getCustomerId() + "/organization";
        ResponseEntity<?> response = sendRequest(restTemplate, apiConfiguration.getApiHost() + api, HttpMethod.POST, requestEntity, Organization.class);

        if (response.getStatusCode().equals(HttpStatus.OK)) {
            Organization organization = (Organization) response.getBody();
            if (organization != null) {
                result = new Result<>(true, "Successfully Created Organization for Customer: " + customer.getName() +
                        " and OrganizationId: " + organization.getOrganizationId(), (Organization) response.getBody());
                updateCustomerOrganizationState(customer.getCustomerId(), result.getEntity().getOrganizationId());
            }
        }

        return result;
    }


    /**
     * Adds the given user to a customer with the given identifier according to this API.
     * <a href="https://artifacts.bos.rapid7.com/nexus/content/sites/razor-site/platform-public-api-app/restdocs/api-guide.html#_add_user_to_customer">Add User to Customer</a>
     *
     * @param customerId the Customer's Identifier to add the user to
     * @param user       the User record to add to the Customer.
     * @return the result of adding the given User to the Customer
     * @throws RetryableException
     */
    @Retryable(value = {RetryableException.class}, maxAttempts = 4, backoff = @Backoff(delay = 15000))
    public Result<User> addUserToCustomer(String customerId, UserInput user) throws RetryableException {

        Result<User> result;

        // Create payload for request
        HttpHeaders headers = getRequestHeader(apiConfiguration, false);
        JSONObject request = new JSONObject();
        request.put("email", user.getEmail());
        request.put("firstName", user.getFirstName());
        request.put("lastName", user.getLastName());
        request.put("platformAdmin", user.isPlatformAdmin());
        HttpEntity<String> requestEntity = new HttpEntity<>(request.toString(), headers);

        // Send API Request to create customer
        final String api = "api/1/customers/" + customerId + "/user";
        ResponseEntity<?> response = sendRequest(restTemplate, apiConfiguration.getApiHost() + api, HttpMethod.POST, requestEntity, User.class);

        if (response.getStatusCode().equals(HttpStatus.OK)) {
            result = new Result<>(true, "Successfully added user: " + user.getEmail(), (User) response.getBody());
        } else if (response.getStatusCode().equals(HttpStatus.CONFLICT)) {
            User existingUser;
            UserProfile userProfile = userService.getUserProfile(customerId, user.getEmail());
            if (userProfile != null) {
                existingUser = new User(userProfile.getUserId(), null, user.getEmail(), "");
                return new Result<>(true, "An User with email: " + user.getEmail() + " already exists in this Customer...", existingUser);
            } else {
                return new Result<>(false, "WARNING: An User Account for: " + user.getEmail() + " already exists but it doesn't belong to this customer. Skipping user...", null);
            }
        } else {
            result = new Result<>(false, "Unexpected error when adding user: " + user.getEmail(), null);
        }

        return result;
    }

    /**
     * Adds the list of roles to the User.
     * <a href="https://api.ipims-int-1.us-east-1.ipims-staging.r7ops.com/docs/asgard/swagger-ui.html?urls.primaryName=class3-apis#/user-controller/addRoleToUserUsingPATCH_1">Add Role(s) to User</a>
     *
     * @param customerId    the Customer Identifier
     * @param userId        The User identifier to add the roles to
     * @param roles         the list of roles that belong to the customer.
     * @param userRoleNames the list of roles to add to the user.
     * @return the result of adding the roles to the user.
     * @throws RetryableException
     */
    @Retryable(value = {RetryableException.class}, maxAttempts = 4, backoff = @Backoff(delay = 15000))
    public Result<User> addRolesToUser(String customerId, String userId, Map<String, Role> roles, List<String> userRoleNames) throws RetryableException {

        Result<User> result;

        List<String> roleIds = new ArrayList<>();
        for (String role : userRoleNames) {
            Role mappedRole = roles.get(role);
            if (mappedRole != null) {
                roleIds.add(mappedRole.getRoleId());
            }
        }

        // Create payload for request
        HttpHeaders headers = getRequestHeader(apiConfiguration, true);
        JSONObject request = new JSONObject();
        request.put("roleIds", roleIds);
        HttpEntity<String> requestEntity = new HttpEntity<>(request.toString(), headers);

        // Send API Request to create customer
        final String api = "rbac/v1/customers/" + customerId + "/users/" + userId + "/roles";
        ResponseEntity<?> response = sendRequest(restTemplate, apiConfiguration.getApiHost() + api, HttpMethod.PATCH, requestEntity, Object.class);

        if (response.getStatusCode().equals(HttpStatus.OK)) {
            result = new Result<>(true, "Successfully added user roles", null);
        } else {
            result = new Result<>(false, "Unexpected error when adding user roles.", null);
        }

        return result;
    }

    /////////////////////////////
    // PRIVATE HELPER METHODS //
    /////////////////////////////

    private Customer getCustomerById(String id) {
        // Create payload for request
        HttpHeaders headers = getRequestHeader(apiConfiguration, false);
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        Customer customer = null;

        // Send API Request to create customer
        final String api = "api/1/customers/" + id;
        ResponseEntity<?> response = restTemplate.exchange(apiConfiguration.getApiHost() + api, HttpMethod.GET, requestEntity, Customer.class);

        if (response.getStatusCode().equals(HttpStatus.OK)) {
            customer = (Customer) response.getBody();
        }

        return customer;
    }

    private List<Organization> getOrganizationsForCustomer(String customerId) {
        // Create payload for request
        HttpHeaders headers = getRequestHeader(apiConfiguration, false);
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        List<Organization> organizations = null;

        // Send API Request to create customer
        final String api = "api/1/customers/" + customerId + "/organizations";
        ResponseEntity<?> response = restTemplate.exchange(apiConfiguration.getApiHost() + api, HttpMethod.GET, requestEntity, Organization[].class);

        if (response.getStatusCode().equals(HttpStatus.OK)) {
            organizations = Arrays.asList((Organization[]) response.getBody());
        }

        return organizations;
    }

    private void updateCustomerState(String customerName, String customerId) {
        state.updateCustomerState(customerName + "," + customerId);
        state.addCustomer(customerName, customerId);
    }

    private void updateCustomerOrganizationState(String customerId, String organizationId) {
        state.updateCustomerOrganizationState(customerId + "," + organizationId);
        state.addCustomerOrganization(customerId, organizationId);
    }
}
