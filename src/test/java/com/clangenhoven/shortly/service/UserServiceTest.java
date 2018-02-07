package com.clangenhoven.shortly.service;

import com.clangenhoven.shortly.dao.UserDao;
import com.clangenhoven.shortly.model.CreateUserRequest;
import com.clangenhoven.shortly.model.User;
import org.apache.shiro.authc.credential.PasswordService;
import org.junit.Test;
import ratpack.exec.ExecResult;
import ratpack.test.exec.ExecHarness;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UserServiceTest {

    @Test
    public void getByUsernameDelegatesToUserDao() throws Exception {
        UserDao userDao = mock(UserDao.class);
        PasswordService passwordService = mock(PasswordService.class);
        UserService service = new UserService(userDao, passwordService);

        when(userDao.getByUsername("username")).thenReturn(Optional.empty());

        try (ExecHarness harness = ExecHarness.harness()) {
            ExecResult<Optional<User>> username = harness.yield(execution -> service.getByUsername("username"));
            assertEquals(username.getValue(), Optional.empty());
        }
    }

    @Test
    public void createUserHashesPassword() throws Exception {
        UserDao userDao = mock(UserDao.class);
        PasswordService passwordService = mock(PasswordService.class);
        UserService service = new UserService(userDao, passwordService);

        when(passwordService.encryptPassword("password")).thenReturn("hashedPassword");
        when(userDao.getByUsername("username")).thenReturn(Optional.empty());

        try (ExecHarness harness = ExecHarness.harness()) {
            harness.yield(execution -> service.createUser(new CreateUserRequest("username", "password")));
        }

        verify(userDao, times(1)).insertUser("username", "hashedPassword");
    }
}
