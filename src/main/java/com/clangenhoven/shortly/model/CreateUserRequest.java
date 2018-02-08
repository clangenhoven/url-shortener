package com.clangenhoven.shortly.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.Length;

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
    @Length(min = 3, max = 50)
    public String getUsername() {
        return username;
    }

    @JsonProperty
    @NotNull
    @Length(min = 6, max = 50)
    public String getPassword() {
        return password;
    }
}
