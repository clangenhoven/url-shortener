package com.clangenhoven.shortly.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;

public class CreateUserRequest {

    private String username;
    private String password;

    public CreateUserRequest() {
    }

    public CreateUserRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @JsonProperty
    @NotNull
    public String getUsername() {
        return username;
    }

    @JsonProperty
    @NotNull
    public String getPassword() {
        return password;
    }
}
