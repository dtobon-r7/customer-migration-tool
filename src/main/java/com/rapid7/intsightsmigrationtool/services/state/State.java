package com.rapid7.intsightsmigrationtool.services.state;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

@Component
public class State {

    /**
     * Map of Customer names to IDs
     */
    private Map<String, String> migratedCustomers;

    /**
     * Map of customer IDs to customer Organization IDs
     */
    private Map<String, String> migratedCustomerOrganizations;

    /**
     * Map of organization IDs to product Tokens
     */
    private Map<String, String> migratedOrgProducts;

    private static final String customerStateFileLocation = System.getProperty("user.dir") + "/r7-state/customers.csv";

    private static final String customerOrganizationStateFileLocation = System.getProperty("user.dir") + "/r7-state/customerOrganizations.csv";

    private static final String orgProductsStateFileLocation = System.getProperty("user.dir") + "/r7-state/orgProducts.csv";

    private static final String outputStateFileLocation = System.getProperty("user.dir") + "/r7-state/output.csv";

    @PostConstruct
    public void init() {
        migratedCustomers = new HashMap<>();
        initializeStateFile(customerStateFileLocation);
        populateStateCustomers();

        migratedCustomerOrganizations = new HashMap<>();
        initializeStateFile(customerOrganizationStateFileLocation);
        populateStateCustomerOrganizations();

        migratedOrgProducts = new HashMap<>();
        initializeStateFile(orgProductsStateFileLocation);
        populateStateOrgProducts();

        initializeStateFile(outputStateFileLocation);
    }

    public void updateCustomerState(String line) {
        writeToStateFile(customerStateFileLocation, line);
    }

    public void updateCustomerOrganizationState(String line) {
        writeToStateFile(customerOrganizationStateFileLocation, line);
    }

    public void updateOrgProductState(String line) {
        writeToStateFile(orgProductsStateFileLocation, line);
    }

    public void updateOutputFile(String line) {
        writeToStateFile(outputStateFileLocation, line);
    }


    private void writeToStateFile(String fileLocation, String line) {
        try (FileWriter writer = new FileWriter(fileLocation, true)) {
            writer.append(line).append("\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean hasCustomerBeenMigrated(String customerName) {
        return migratedCustomers.containsKey(customerName);
    }

    public void addCustomer(String customerName, String customerId) {
        migratedCustomers.put(customerName, customerId);
    }

    public String getCustomerId(String customerName) {
        return migratedCustomers.get(customerName);
    }


    public boolean hasOrganizationForCustomerBeenCreated(String customerId) {
        return migratedCustomerOrganizations.containsKey(customerId);
    }

    public void addCustomerOrganization(String customerId, String organizationId) {
        migratedCustomerOrganizations.put(customerId, organizationId);
    }

    public void addOrgProduct(String organizationId, String productToken) {
        migratedOrgProducts.put(organizationId, productToken);
    }

    public boolean hasOrgProductBeenCreated(String organizationId) {
        return migratedOrgProducts.containsKey(organizationId);
    }

    public String getOrganizationId(String customerId) {
        return migratedCustomerOrganizations.get(customerId);
    }

    private void populateStateCustomers() {
        readStateLines(customerStateFileLocation, migratedCustomers);
    }

    private void populateStateCustomerOrganizations() {
        readStateLines(customerOrganizationStateFileLocation, migratedCustomerOrganizations);
    }

    private void populateStateOrgProducts() {
        readStateLines(orgProductsStateFileLocation, migratedOrgProducts);
    }

    private void readStateLines(String stateFileLocation, Map<String, String> stateMap) {
        try (BufferedReader reader = new BufferedReader(new FileReader(stateFileLocation))) {
            String line = reader.readLine();
            while (line != null) {
                String[] components = line.split(",");
                stateMap.put(components[0], components[1]);
                line = reader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initializeStateFile(String location) {
        File stateFile = new File(location);
        try {
            if (!stateFile.getParentFile().exists()) {
                stateFile.getParentFile().mkdirs();
            }
            if (!stateFile.exists()) {
                stateFile.createNewFile();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
