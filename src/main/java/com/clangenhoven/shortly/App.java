package com.clangenhoven.shortly;

import com.clangenhoven.shortly.client.LifecycleAwareRedisClient;
import ratpack.guice.Guice;
import ratpack.server.RatpackServer;
import ratpack.session.SessionModule;

public class App {

    public static void main(String... args) throws Exception {
        RatpackServer.start(server -> server
                .registry(Guice.registry(bindings -> bindings
                        .module(AppModule.class)
                        .module(SessionModule.class)
                        .bind(LifecycleAwareRedisClient.class)))
                .handlers(chain -> chain
                        .get(ctx -> ctx.render("Hello World!"))
                        .get(":name", ctx -> ctx.render("Hello " + ctx.getPathTokens().get("name") + "!"))
                )
        );
    }
}
