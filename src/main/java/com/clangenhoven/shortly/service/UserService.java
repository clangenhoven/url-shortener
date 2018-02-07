package com.clangenhoven.shortly.service;

import com.clangenhoven.shortly.dao.UserDao;
import com.clangenhoven.shortly.model.CreateUserRequest;
import com.clangenhoven.shortly.model.User;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.shiro.authc.credential.PasswordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ratpack.exec.Blocking;
import ratpack.exec.Promise;

import java.util.Optional;

@Singleton
public class UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    private final UserDao userDao;
    private final PasswordService passwordService;

    @Inject
    public UserService(UserDao userDao, PasswordService passwordService) {
        this.userDao = userDao;
        this.passwordService = passwordService;
    }

    public Promise<Boolean> createUser(CreateUserRequest request) {
        return Blocking.get(() -> {
            try {
                userDao.insertUser(request.getUsername(), passwordService.encryptPassword(request.getPassword()));
                return true;
            } catch (Exception e) {
                LOGGER.error("Caught exception while creating user", e);
                return false;
            }
        });
    }

    public Promise<Optional<User>> getByUsername(String username) {
        return Blocking.get(() -> userDao.getByUsername(username));
    }
}
