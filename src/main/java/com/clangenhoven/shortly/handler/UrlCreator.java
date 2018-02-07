package com.clangenhoven.shortly.handler;

import com.clangenhoven.shortly.handler.validation.JsonValidator;
import com.clangenhoven.shortly.model.CreateUrlRequest;
import com.clangenhoven.shortly.service.UrlService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.pac4j.core.profile.UserProfile;
import ratpack.handling.Context;
import ratpack.handling.Handler;

@Singleton
public class UrlCreator extends JsonValidator<CreateUrlRequest> implements Handler {

    private final UrlService urlService;

    @Inject
    public UrlCreator(UrlService urlService) {
        super(CreateUrlRequest.class);
        this.urlService = urlService;
    }

    @Override
    protected void handle(Context ctx, CreateUrlRequest request) {
        UserProfile profile = ctx.get(UserProfile.class);
        Long id = profile.getAttribute("id", Long.class);
        urlService.createUrl(request, id).then(result -> {
            if (result.isPresent()) {
                ctx.render(result.get());
            } else {
                ctx.getResponse().status(400).send();
            }
        });
    }
}
