package com.clangenhoven.shortly.handler;

import com.clangenhoven.shortly.model.Url;
import com.clangenhoven.shortly.service.UrlService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import ratpack.handling.Context;
import ratpack.handling.Handler;

import static ratpack.jackson.Jackson.json;

@Singleton
public class UrlHandler implements Handler {

    private final UrlService urlService;

    @Inject
    public UrlHandler(UrlService urlService) {
        this.urlService = urlService;
    }

    @Override
    public void handle(Context ctx) throws Exception {
        urlService.lookupUrl(ctx.getPathTokens().get("shortUrl"), result -> {
            if (result.isPresent()) {
                Url url = result.get();
                if (ctx.getRequest().getQueryParams().containsKey("info")) {
                    ctx.render(json(url));
                } else {
                    ctx.redirect(url.getUrl());
                }
            } else {
                ctx.notFound();
            }
        });
    }
}
