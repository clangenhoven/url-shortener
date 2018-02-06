package com.clangenhoven.shortly.model;

public class User {

    private final long id;
    private final String username;
    private final String hashedPassword;

    public User(long id, String username, String hashedPassword) {
        this.id = id;
        this.username = username;
        this.hashedPassword = hashedPassword;
    }

    public long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }
}
