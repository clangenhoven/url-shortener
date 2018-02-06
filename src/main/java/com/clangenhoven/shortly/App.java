package com.clangenhoven.shortly;

import com.clangenhoven.shortly.client.LifecycleAwareRedisClient;
import com.clangenhoven.shortly.config.DevelopmentConfig;
import com.clangenhoven.shortly.handler.UrlCreator;
import com.clangenhoven.shortly.handler.UrlHandler;
import com.clangenhoven.shortly.handler.UrlLister;
import com.clangenhoven.shortly.handler.UrlRedirector;
import com.clangenhoven.shortly.handler.UserCreator;
import org.pac4j.http.client.direct.DirectBasicAuthClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ratpack.groovy.template.TextTemplateModule;
import ratpack.guice.Guice;
import ratpack.pac4j.RatpackPac4j;
import ratpack.server.RatpackServer;
import ratpack.session.SessionModule;

import java.io.File;

public class App {

    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    public static void main(String... args) throws Exception {
        RatpackServer.start(server -> server
                .serverConfig(c -> c
                        .baseDir(new File("src/main").getAbsoluteFile())
                        .port(8080))
                .registry(Guice.registry(bindings -> bindings
                        .module(TextTemplateModule.class)
                        .module(SessionModule.class)
                        .module(DevelopmentConfig.class) // todo-chris: select config based on env variable
                        .module(AppModule.class)
                        .bind(LifecycleAwareRedisClient.class)))
                .handlers(chain -> chain
                        .all(ctx -> RatpackPac4j.authenticator(ctx.get(DirectBasicAuthClient.class)).handle(ctx))
                        .get("u/:shortUrl", UrlRedirector.class)
                        .post("createUser", UserCreator.class)
                        .prefix("createUrl", c -> c
                                .all(RatpackPac4j.requireAuth(DirectBasicAuthClient.class))
                                .post(UrlCreator.class)
                        )
                        .prefix("i", c -> c
                                .all(RatpackPac4j.requireAuth(DirectBasicAuthClient.class))
                                .get(UrlLister.class)
                                .get(":shortUrl", UrlHandler.class)
                        )
                )
        );
    }
}
