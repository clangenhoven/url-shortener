package com.clangenhoven.shortly.config;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

public class DevelopmentConfig extends AbstractModule {

    @Override
    protected void configure() {
        bind(String.class).annotatedWith(Names.named("redisUri")).toInstance("redis://127.0.0.1");
        bind(String.class).annotatedWith(Names.named("jdbcDriver")).toInstance("org.postgresql.Driver");
        bind(String.class).annotatedWith(Names.named("jdbcUrl")).toInstance("jdbc:postgresql://127.0.0.1:5432/shortly");
        bind(String.class).annotatedWith(Names.named("jdbcUser")).toInstance("shortly");
        bind(String.class).annotatedWith(Names.named("jdbcPassword")).toInstance("6t]^yBgX?t{8b5k2?(3D");
    }
}
