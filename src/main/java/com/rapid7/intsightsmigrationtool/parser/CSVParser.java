package com.rapid7.intsightsmigrationtool.parser;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CSVParser {

  public Map<String, CustomerInput> parse(File csv) {

    Map<String, CustomerInput> customers = new HashMap<>();

    List<String[]> input;
    try (CSVReader reader = new CSVReader(new FileReader(csv))) {
      input = reader.readAll();
    } catch (IOException | CsvException e) {
      throw new RuntimeException(e);
    }

    //Remove header row
    input.remove(0);

    input.forEach(
        line -> {
          String customerName = line[IntSightsInputKey.CUSTOMER_NAME.getIndex()];

          CustomerInput customer =
                  customers.getOrDefault(
                  customerName,
                  new CustomerInput(
                      line[IntSightsInputKey.ACCOUNT_ID.getIndex()],
                      customerName,
                      line[IntSightsInputKey.ORGANIZATION_NAME.getIndex()],
                      line[IntSightsInputKey.ORGANIZATION_REGION.getIndex()].toLowerCase(),
                      line[IntSightsInputKey.PRODUCT_CODE.getIndex()].toUpperCase(),
                      new HashMap<>()));

          UserInput user = new UserInput();
          user.setEmail(line[IntSightsInputKey.USER_EMAIL.getIndex()]);
          user.setFirstName(line[IntSightsInputKey.USER_FIRST_NAME.getIndex()]);
          user.setLastName(line[IntSightsInputKey.USER_LAST_NAME.getIndex()]);
          user.setPlatformAdmin(
              Boolean.parseBoolean(line[IntSightsInputKey.USER_PLATFORM_ADMIN_STATUS.getIndex()].toLowerCase()));
          user.setRoles(
              Arrays.asList(line[IntSightsInputKey.USER_PRODUCT_ROLES.getIndex()].split(",")));

          customer.getUsers().put(user.getEmail(), user);
            customers.put(customerName, customer);
        });

    return customers;
  }
}
