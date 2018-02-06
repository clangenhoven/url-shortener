package com.clangenhoven.shortly.auth;

import com.clangenhoven.shortly.dao.UserDao;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.shiro.authc.credential.PasswordService;
import org.pac4j.core.exception.CredentialsException;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.util.CommonHelper;
import org.pac4j.http.credentials.UsernamePasswordCredentials;
import org.pac4j.http.credentials.authenticator.UsernamePasswordAuthenticator;

@Singleton
public class Authenticator implements UsernamePasswordAuthenticator {

    private final UserDao userDao;
    private final PasswordService passwordService;

    @Inject
    public Authenticator(UserDao userDao, PasswordService passwordService) {
        this.userDao = userDao;
        this.passwordService = passwordService;
    }

    @Override
    public void validate(final UsernamePasswordCredentials credentials) {
        if (credentials == null) {
            throw new CredentialsException("No credential");
        }
        String username = credentials.getUsername();
        String password = credentials.getPassword();
        if (CommonHelper.isBlank(username)) {
            throw new CredentialsException("Username cannot be blank");
        }
        if (CommonHelper.isBlank(password)) {
            throw new CredentialsException("Password cannot be blank");
        }
        CommonProfile profile = userDao.getByUsername(username)
                .filter(u -> passwordService.passwordsMatch(password, u.getHashedPassword()))
                .map(u -> {
                    final CommonProfile p = new CommonProfile();
                    p.setId(username);
                    p.addAttribute(CommonProfile.USERNAME, username);
                    p.addAttribute("id", u.getId());
                    return p;
                })
                .orElseThrow(() -> new CredentialsException("Invalid credentials provided"));
        credentials.setUserProfile(profile);
    }
}
