package com.clangenhoven.shortly.handler;

import com.clangenhoven.shortly.service.UrlService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.pac4j.core.profile.UserProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ratpack.handling.Context;
import ratpack.handling.Handler;

import static ratpack.jackson.Jackson.json;

@Singleton
public class UrlLister implements Handler {

    private static final Logger LOGGER = LoggerFactory.getLogger(UrlLister.class);

    private final UrlService urlService;

    @Inject
    public UrlLister(UrlService urlService) {
        this.urlService = urlService;
    }

    @Override
    public void handle(Context ctx) throws Exception {
        UserProfile profile = ctx.get(UserProfile.class);
        Long id = profile.getAttribute("id", Long.class);
        urlService.listUrls(id).then(urls -> ctx.render(json(urls)));
    }
}
