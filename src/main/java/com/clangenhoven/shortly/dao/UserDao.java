package com.clangenhoven.shortly.dao;

import com.clangenhoven.shortly.model.User;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.Optional;

public interface UserDao {

    @SqlUpdate("insert into user (username, hashed_password) values (:username, :hashedPassword)")
    void insertUser(@Bind("username") String username, @Bind("hashedPassword") String hashedPassword);

    @SqlQuery("select * from user where username = :username")
    Optional<User> getByUsername(@Bind("username") String username);
}
