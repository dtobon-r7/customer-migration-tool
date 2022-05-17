package com.rapid7.intsightsmigrationtool.services.migrators;

import com.rapid7.intsightsmigrationtool.gui.GUI;
import com.rapid7.intsightsmigrationtool.parser.CustomerInput;
import com.rapid7.intsightsmigrationtool.services.*;
import com.rapid7.intsightsmigrationtool.services.dto.*;
import com.rapid7.intsightsmigrationtool.services.state.State;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.Map;

/**
 * Orchestrator service to perform IntSights customer Migrations.
 */
@AllArgsConstructor
@Component
public class IntSightsMigratorService {

    private static final Logger logger = LoggerFactory.getLogger(IntSightsMigratorService.class);

    private CustomerService customerService;
    private OrganizationService organizationService;
    private ProductService productService;
    private UserService userService;
    private State state;

    /////////////////////////////
    //     PUBLIC  METHODS     //
    /////////////////////////////

    /**
     * Performs the migration of the given customers in to the iPIMS system.
     *
     * @param customers the map of customers to migration. Map of Customer Names to CustomerInput records to migrate.
     * @param gui       the instance of the GUI to update as the records are created.
     */
    public void migrate(Map<String, CustomerInput> customers, GUI gui) {

        logger.info("Initiating Migration for {} customers", customers.size());

        // Ensure keys work before performing migration to avoid failing halfway through.
        if (!testPublicApiKey(gui) || !testRBACApiKey(gui)) {
            return;
        }

        // Loop through all the given customers and create the records needed.
        customers.values().forEach(customerToMigrate -> {
            try {
                // Create the Customer entity in the iPIMS system.
                Result<Customer> customerResult = customerService.createCustomer(customerToMigrate.getCustomerName());
                Customer customer = customerResult.getEntity();
                gui.updateOutput(customerResult.getMessage());

                // Create the Organization for the Customer
                if (customer != null) {
                    Result<Organization> organizationResult = customerService.createOrgForCustomer(customer, customerToMigrate.getOrganizationName(), customerToMigrate.getOrganizationRegion());
                    Organization organization = organizationResult.getEntity();
                    gui.updateOutput(organizationResult.getMessage());

                    // Create OrgProduct for Organization
                    if (organization != null) {
                        Result<OrgProduct> orgProductResult = organizationService.createOrgProductForOrganization(organization.getOrganizationId(), customerToMigrate.getProductCode(), "PURCHASED");
                        OrgProduct orgProduct = orgProductResult.getEntity();
                        gui.updateOutput(orgProductResult.getMessage());

                        if (orgProduct != null) {
                            Map<String, Role> productRoles = productService.getSuggestedRolesForProduct(orgProduct.getProductCode());

                            customerToMigrate.getUsers().forEach((s, userInput) -> {
                                try {
                                    // Adds the User to the Customer entity.
                                    Result<User> userResult = customerService.addUserToCustomer(customer.getCustomerId(), userInput);
                                    gui.updateOutput(userResult.getMessage());

                                    if (userResult.isSuccess()) {
                                        // Add User to OrgProduct
                                        Result<?> userOrgProduct = organizationService.addUserToOrgProduct(organization.getOrganizationId(),
                                                orgProduct.getProductToken(), userInput.getEmail(), userInput.isPlatformAdmin());
                                        gui.updateOutput(userOrgProduct.getMessage());

                                        // Add Roles to user.
                                        User user = userResult.getEntity();
                                        if (user != null) {
                                            Result<?> addRolesResult = customerService.addRolesToUser(customer.getCustomerId(), user.getUserId(), productRoles, userInput.getRoles());
                                            gui.updateOutput(addRolesResult.getMessage());

                                            outputLine(customerToMigrate.getAccountID(), customerToMigrate.getCustomerName(),
                                                    customerResult.isSuccess(), customer.getCustomerId(), organization.getOrganizationName(), organizationResult.isSuccess(),
                                                    organization.getOrganizationId(), organization.getRegionCode(), orgProduct.getProductCode(), orgProductResult.isSuccess(), orgProduct.getProductToken(),
                                                    userInput.getEmail(), userResult.isSuccess(), userInput.getFirstName(), userInput.getLastName(), userInput.isPlatformAdmin(), userInput.getRoles());

                                        } else {
                                            gui.updateOutput("FAILED: Error occurred while trying to get User entity by email.");
                                        }
                                    } else {
                                        logger.warn("Failed to add user {} to Customer {}", userInput.getEmail(), customerToMigrate.getCustomerName());
                                    }
                                } catch (RetryableException e) {
                                    gui.updateOutput("Retry has reached maximum calls allowed. ");
                                }
                            });
                        }
                    }
                } else {
                    logger.error("Could not retrieve Customer record.");
                }

            } catch (RetryableException e) {
                gui.updateOutput("Retry has reached maximum calls allowed. ");
                throw new RuntimeException(e);
            }
        });

        gui.updateOutput("Migration Completed!");
        logger.info("Completed Migration.");
    }

    //////////////////////////////
    //  PRIVATE HELPER METHODS  //
    //////////////////////////////

    private void outputLine(String accountId, String customerName, boolean customerCreated, String customerId, String organizationName,
                            boolean organizationCreated, String organizationId, String organizationRegion, String productCode,
                            boolean productCreated, String productToken, String userEmail, boolean createdSuccessful,
                            String userFirstName, String userLastName, boolean isUserPlatformAdmin, List<String> productRoles) {
        String line = accountId + "," + customerName + "," + customerCreated + "," + customerId + "," + organizationName + "," + organizationCreated
                + "," + organizationId + "," + organizationRegion + "," + productCode + "," + productCreated + "," + productToken + ","
                + userEmail + "," + createdSuccessful + "," + userFirstName + "," + userLastName + ","
                + isUserPlatformAdmin + ",\"" + String.join(",", productRoles) + "\"";
        state.updateOutputFile(line);
    }

    private boolean testPublicApiKey(GUI gui) {
        try {
            if (userService.testPublicApiKey()) {
                gui.updateOutput("Public API Key check passed!");
            }
        } catch (HttpClientErrorException e) {
            logger.error("Failed Public API Key check.", e);
            gui.updateOutput("Failed Public API Key check, Please make sure API HOST URL and Public API key are valid.");
            return false;
        }
        return true;
    }

    private boolean testRBACApiKey(GUI gui) {
        try {
            if (userService.testRBACKey()) {
                gui.updateOutput("RBAC API Key check passed!");
            }
        } catch (HttpClientErrorException e) {
            logger.error("Failed RBAC API Key check.", e);
            gui.updateOutput("Failed RBAC Key check, Please make sure API HOST and API key are valid.");
            return false;
        }
        return true;
    }
}
