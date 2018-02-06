package com.clangenhoven.shortly.handler.validation;

import ratpack.exec.Promise;
import ratpack.handling.Context;
import ratpack.handling.Handler;
import ratpack.jackson.Jackson;

import javax.validation.Validator;

public abstract class JsonValidator<T> implements Handler {

    private final Class<T> type;

    public JsonValidator(Class<T> type) {
        this.type = type;
    }

    @Override
    public final void handle(Context ctx) throws Exception {
        parseAndValidate(ctx, type).then(obj -> this.handle(ctx, obj));
    }

    protected abstract void handle(Context ctx, T object);

    private Promise<T> parseAndValidate(Context ctx, Class<T> type) {
        return ctx.parse(Jackson.fromJson(type))
                .route(obj -> ctx.get(Validator.class).validate(obj).size() > 0,
                        ex -> ctx.clientError(422)) /* failed validation */
                .onError(ex -> ctx.clientError(400)); /* failed to parse body */

    }
}
