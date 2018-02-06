package com.clangenhoven.shortly.service;

import com.clangenhoven.shortly.dao.UserDao;
import com.clangenhoven.shortly.model.CreateUserRequest;
import com.clangenhoven.shortly.model.User;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.shiro.authc.credential.PasswordService;
import ratpack.exec.Blocking;

import java.util.Optional;
import java.util.function.Consumer;

@Singleton
public class UserService {

    private final UserDao userDao;
    private final PasswordService passwordService;

    @Inject
    public UserService(UserDao userDao, PasswordService passwordService) {
        this.userDao = userDao;
        this.passwordService = passwordService;
    }

    public void createUser(CreateUserRequest request, Consumer<Boolean> callback) {
        Blocking.get(() -> {
            userDao.insertUser(request.getUsername(), passwordService.encryptPassword(request.getPassword()));
            return true;
        }).then(callback::accept);
    }

    public Optional<User> getByUsername(String username) {
        return userDao.getByUsername(username);
    }
}
