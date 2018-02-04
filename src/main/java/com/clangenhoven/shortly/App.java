package com.clangenhoven.shortly;

import com.clangenhoven.shortly.client.LifecycleAwareRedisClient;
import com.clangenhoven.shortly.config.DevelopmentConfig;
import com.clangenhoven.shortly.handler.UrlHandler;
import ratpack.guice.Guice;
import ratpack.server.RatpackServer;

import java.io.File;

public class App {

    public static void main(String... args) throws Exception {
        RatpackServer.start(server -> server
                .serverConfig(c -> c
                        .baseDir(new File("src/main").getAbsoluteFile())
                        .port(8080))
                .registry(Guice.registry(bindings -> bindings
                        .module(DevelopmentConfig.class) // todo-chris: select config based on env variable
                        .module(AppModule.class)
                        .bind(LifecycleAwareRedisClient.class)))
                .handlers(chain -> chain
                        .get(":shortUrl", UrlHandler.class)
                )
        );
    }
}
