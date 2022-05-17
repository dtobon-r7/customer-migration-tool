package com.rapid7.intsightsmigrationtool.gui;

import com.rapid7.intsightsmigrationtool.parser.CSVParser;
import com.rapid7.intsightsmigrationtool.parser.CustomerInput;
import com.rapid7.intsightsmigrationtool.services.ApiConfiguration;
import com.rapid7.intsightsmigrationtool.services.migrators.IntSightsMigratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.util.Map;


@Component
public class GUI extends JFrame {

    @Autowired
    private IntSightsMigratorService intSightsMigrator;

    @Autowired
    private CSVParser csvParser;

    @Autowired
    private ApiConfiguration apiConfiguration;

    private final JLabel inputFileLabel = new JLabel("Input File:");

    private final JTextField apiHostUrlTextField = new JTextField();

    private final JPasswordField apiKeyTextField = new JPasswordField(10);


    private final JPasswordField rbacApiKeyTextField = new JPasswordField(10);

    private final JTextField apiConsumerTextField = new JTextField(10);

    private final JTextArea inputEntitiesTextArea = new JTextArea(10, 70);

    private final JButton migrateButton = new JButton("Migrate");

    private final JTextArea resultsTextArea = new JTextArea(20, 70);

    private Map<String, CustomerInput> customers;

    private File inputCsvFile;

    public GUI() {
        super("Customer Migration Tool");

        JButton fileSelectionButton = new JButton("Select File");
        fileSelectionButton.addActionListener(e -> selectFile());
        migrateButton.addActionListener(e -> migrate());
        migrateButton.setEnabled(false);
        inputEntitiesTextArea.setEditable(false);
        resultsTextArea.setEditable(false);

        JPanel pane = new JPanel(new GridBagLayout());
        pane.setLayout(new GridBagLayout());
        pane.setBorder(new EmptyBorder(15, 20, 0, 10));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.PAGE_START;
        resultsTextArea.setLineWrap(true);
        resultsTextArea.setWrapStyleWord(true);
        JScrollPane resultScroll = new JScrollPane(resultsTextArea);
        resultScroll.setViewportView(resultsTextArea);
        inputEntitiesTextArea.setLineWrap(true);
        inputEntitiesTextArea.setWrapStyleWord(true);
        JScrollPane inputEntitiesScroll = new JScrollPane(inputEntitiesTextArea);
        inputEntitiesScroll.setViewportView(inputEntitiesTextArea);

        // Add Input File Selection Section
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 0;
        constraints.gridy = 0;
        pane.add(inputFileLabel, constraints);

        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        constraints.gridx = 1;
        constraints.gridy = 0;
        pane.add(inputFileLabel, constraints);

        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = .01;
        constraints.gridx = 2;
        constraints.gridy = 0;
        pane.add(fileSelectionButton, constraints);

        // Add API Host URL Section
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weighty = .01;
        constraints.gridx = 1;
        constraints.gridy = 1;
        JLabel rbacHostLabel = new JLabel("API HOST URL: ");
        pane.add(rbacHostLabel, constraints);

        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = .1;
        constraints.gridx = 1;
        constraints.gridy = 2;
        apiHostUrlTextField.setText("https://api.ipims-int-1.us-east-1.ipims-staging.r7ops.com/api/");
        apiHostUrlTextField.setToolTipText("Please enter the RBAC HOST URL");
        pane.add(apiHostUrlTextField, constraints);

        // Add API KEYs Section
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weighty = .01;
        constraints.weightx = .5;
        constraints.gridx = 1;
        constraints.gridy = 3;
        JLabel apiKeyLabel = new JLabel("PUBLIC API KEY: ");
        pane.add(apiKeyLabel, constraints);

        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weighty = .01;
        constraints.weightx = .5;
        constraints.gridx = 1;
        constraints.gridy = 4;
        apiKeyTextField.setMaximumSize(new Dimension(10, 10));
        apiKeyTextField.setText("");
        apiKeyTextField.setToolTipText("Please enter the API Key");
        pane.add(apiKeyTextField, constraints);

        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weighty = .01;
        constraints.weightx = .5;
        constraints.gridx = 1;
        constraints.gridy = 5;
        JLabel rbacApiKeyLabel = new JLabel("RBAC Service Key: ");
        pane.add(rbacApiKeyLabel, constraints);

        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weighty = .01;
        constraints.weightx = .5;
        constraints.gridx = 1;
        constraints.gridy = 6;
        rbacApiKeyTextField.setMaximumSize(new Dimension(10, 10));
        rbacApiKeyTextField.setText("");
        rbacApiKeyTextField.setToolTipText("Please enter the API Key");
        pane.add(rbacApiKeyTextField, constraints);

        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weighty = .01;
        constraints.gridx = 1;
        constraints.gridy = 7;
        JLabel consumerLabel = new JLabel("R7 Consumer: ");
        pane.add(consumerLabel, constraints);

        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weighty = .01;
        constraints.gridx = 1;
        constraints.gridy = 8;
        apiConsumerTextField.setText("xteam-class1-api-gateway-app");
        apiConsumerTextField.setToolTipText("Please enter R7-Consumer header");
        pane.add(apiConsumerTextField, constraints);

        // Add Input Entities Section
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weighty = .01;
        constraints.gridx = 1;
        constraints.gridy = 9;
        constraints.insets = new Insets(5, 5, 3, 0);
        // Input Entities Fields
        JLabel inputEntitiesLabel = new JLabel("Entities to Migrate:");
        pane.add(inputEntitiesLabel, constraints);

        constraints.fill = GridBagConstraints.BOTH;
        constraints.weighty = 1;
        constraints.gridx = 1;
        constraints.gridy = 10;
        constraints.insets = new Insets(0, 3, 0, 0);
        pane.add(inputEntitiesScroll, constraints);

        // Add Migration Result Section
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weighty = .01;
        constraints.gridx = 1;
        constraints.gridy = 11;
        constraints.insets = new Insets(3, 5, 0, 0);
        JLabel migrationResultLabel = new JLabel("Migration Result:");
        pane.add(migrationResultLabel, constraints);

        constraints.fill = GridBagConstraints.BOTH;
        constraints.weighty = 1;
        constraints.gridx = 1;
        constraints.gridy = 12;
        constraints.insets = new Insets(0, 3, 0, 0);
        pane.add(resultScroll, constraints);

        // Add migrate button
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weighty = .01;
        constraints.gridx = 1;
        constraints.gridy = 13;
        constraints.insets = new Insets(0, 3, 10, 5);
        pane.add(migrateButton, constraints);

        this.setResizable(false);
        add(pane);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    public void updateOutput(String line) {
        resultsTextArea.append(line + "\n");
        resultsTextArea.update(resultsTextArea.getGraphics());
    }

    private void selectFile() {
        inputEntitiesTextArea.setText("");
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        int option = fileChooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            inputCsvFile = fileChooser.getSelectedFile();
            inputFileLabel.setText("Selected: " + inputCsvFile.getName());

            customers = csvParser.parse(inputCsvFile);
            customers.forEach((customerName, customer) -> {
                inputEntitiesTextArea.append("\nCustomer: " + customerName + "\n");

                customer.getUsers().forEach((s, userInput) -> inputEntitiesTextArea.append("Email: " + userInput.getEmail() + ", First Name: "
                        + userInput.getFirstName() + ", Last Name: " + userInput.getLastName() + ",  Is Admin: " + userInput.isPlatformAdmin() + ", Roles: "
                        + String.join(", ", userInput.getRoles()) + "\n"));
            });

            migrateButton.setEnabled(true);
        } else {
            inputFileLabel.setText("Open command canceled");
        }
    }

    private void migrate() {

        resultsTextArea.setText("");
        String curatedApiHost = apiHostUrlTextField.getText();
        if (!curatedApiHost.endsWith("/")) {
            curatedApiHost = curatedApiHost + "/";
        }

        apiConfiguration.setApiHost(curatedApiHost);
        apiConfiguration.setApiKey(new String(apiKeyTextField.getPassword()));
        apiConfiguration.setRbacApiKey(new String(rbacApiKeyTextField.getPassword()));
        apiConfiguration.setConsumerHeader(apiConsumerTextField.getText());

        intSightsMigrator.migrate(customers, this);
        migrateButton.setEnabled(false);
    }
}
