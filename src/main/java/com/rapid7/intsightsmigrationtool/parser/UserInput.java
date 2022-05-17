package com.rapid7.intsightsmigrationtool.parser;

import lombok.Data;

import java.util.List;

@Data
public class UserInput {

    private String email;
    private String firstName;
    private String lastName;
    private boolean platformAdmin;
    private List<String> roles;
}
