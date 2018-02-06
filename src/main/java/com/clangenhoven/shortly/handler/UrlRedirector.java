package com.clangenhoven.shortly.handler;

import com.clangenhoven.shortly.service.UrlService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import ratpack.handling.Context;
import ratpack.handling.Handler;

@Singleton
public class UrlRedirector implements Handler {

    private final UrlService urlService;

    @Inject
    public UrlRedirector(UrlService urlService) {
        this.urlService = urlService;
    }

    @Override
    public void handle(Context ctx) throws Exception {
        urlService.lookupUrl(ctx.getPathTokens().get("shortUrl"), result -> {
            if (result.isPresent()) {
                ctx.redirect(result.get().getUrl());
            } else {
                ctx.notFound();
            }
        });
    }
}
