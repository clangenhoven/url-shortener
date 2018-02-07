package com.clangenhoven.shortly.handler;

import com.clangenhoven.shortly.service.UrlService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.pac4j.core.profile.UserProfile;
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
        UserProfile profile = ctx.get(UserProfile.class);
        Long id = profile.getAttribute("id", Long.class);
        urlService.lookupUrlBypassCache(ctx.getPathTokens().get("shortUrl")).then(result -> {
            if (result.isPresent() && id.equals(result.get().getOwnerId())) {
                ctx.render(json(result.get()));
            } else {
                ctx.notFound();
            }
        });
    }
}
