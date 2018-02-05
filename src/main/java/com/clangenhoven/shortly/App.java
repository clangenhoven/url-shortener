package com.clangenhoven.shortly;

import com.clangenhoven.shortly.client.LifecycleAwareRedisClient;
import com.clangenhoven.shortly.config.DevelopmentConfig;
import com.clangenhoven.shortly.handler.UrlCreator;
import com.clangenhoven.shortly.handler.UrlHandler;
import com.clangenhoven.shortly.handler.UrlLister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ratpack.groovy.template.TextTemplateModule;
import ratpack.guice.Guice;
import ratpack.server.RatpackServer;

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
                        .module(DevelopmentConfig.class) // todo-chris: select config based on env variable
                        .module(AppModule.class)
                        .bind(LifecycleAwareRedisClient.class)))
                .handlers(chain -> chain
                        .post("create", UrlCreator.class)
                        .prefix("u", c -> c
                                .get(":shortUrl", UrlHandler.class)
                                .get(UrlLister.class)
                        )
                )
        );
    }
}
