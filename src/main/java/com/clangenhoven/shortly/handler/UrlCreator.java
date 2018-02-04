package com.clangenhoven.shortly.handler;

import com.clangenhoven.shortly.model.CreateUrlRequest;
import com.clangenhoven.shortly.service.UrlService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import ratpack.handling.Context;
import ratpack.handling.Handler;

import static ratpack.jackson.Jackson.fromJson;

@Singleton
public class UrlCreator implements Handler {

    private final UrlService urlService;

    @Inject
    public UrlCreator(UrlService urlService) {
        this.urlService = urlService;
    }

    @Override
    public void handle(Context ctx) throws Exception {
        ctx.parse(fromJson(CreateUrlRequest.class))
                .then(req -> urlService.createUrl(req, 1, result -> {
                    if (result.isPresent()) {
                        ctx.render(result.get());
                    } else {
                        ctx.getResponse().status(400).send();
                    }
                }));
    }
}
