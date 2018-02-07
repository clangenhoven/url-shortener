package com.clangenhoven.shortly.handler;

import com.clangenhoven.shortly.handler.validation.JsonValidator;
import com.clangenhoven.shortly.model.CreateUserRequest;
import com.clangenhoven.shortly.service.UserService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import ratpack.handling.Context;
import ratpack.handling.Handler;
import ratpack.http.Response;

@Singleton
public class UserCreator extends JsonValidator<CreateUserRequest> implements Handler {

    private final UserService userService;

    @Inject
    public UserCreator(UserService userService) {
        super(CreateUserRequest.class);
        this.userService = userService;
    }

    @Override
    protected void handle(Context ctx, CreateUserRequest request) {
        userService.createUser(request).then(success -> {
            Response response = ctx.getResponse();
            if (success) {
                response.status(200).send();
            } else {
                response.status(400).send();
            }
        });
    }
}
