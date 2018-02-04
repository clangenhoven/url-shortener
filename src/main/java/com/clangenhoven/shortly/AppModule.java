package com.clangenhoven.shortly;

import com.clangenhoven.shortly.handler.UrlHandler;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.clangenhoven.shortly.dao.UrlDao;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import com.clangenhoven.shortly.model.Url;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import ratpack.error.ClientErrorHandler;
import ratpack.error.ServerErrorHandler;

import javax.sql.DataSource;

import static ratpack.groovy.Groovy.groovyTemplate;

public class AppModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(UrlHandler.class);
        bind(ClientErrorHandler.class).toInstance((ctx, statusCode) -> {
            ctx.getResponse().status(statusCode);
            if (statusCode == 404) {
                ctx.render(groovyTemplate("error404.html"));
            } else if (statusCode == 401) {
                ctx.render(groovyTemplate("error401.html"));
            } else if (statusCode == 403) {
                ctx.render(groovyTemplate("error403.html"));
            }
        });
        bind(ServerErrorHandler.class).toInstance((ctx, error) -> {
            ctx.render(groovyTemplate("error500.html"));
        });
    }

    @Provides
    @Singleton
    private StatefulRedisConnection<String, String> redisConnection(RedisClient redisClient) {
        return redisClient.connect();
    }

    @Provides
    @Singleton
    private RedisClient redisClient(@Named("redisUri") String uri) {
        return RedisClient.create(uri);
    }

    @Provides
    @Singleton
    private UrlDao urlDAO(Jdbi jdbi) {
        return jdbi.onDemand(UrlDao.class);
    }

    @Provides
    @Singleton
    private Jdbi jdbi(DataSource dataSource) {
        Jdbi jdbi = Jdbi.create(dataSource);
        jdbi.installPlugin(new SqlObjectPlugin());
        jdbi.registerRowMapper(Url.class, (rs, ctx) ->
                new Url(rs.getLong("id"),
                        rs.getString("url"),
                        rs.getString("short_url"),
                        rs.getLong("owner_id")));
        return jdbi;
    }

    @Provides
    @Singleton
    private DataSource getDataSource(@Named("jdbcDriver") String driver,
                                     @Named("jdbcUrl") String url,
                                     @Named("jdbcUser") String user,
                                     @Named("jdbcPassword") String password) throws Exception {
        ComboPooledDataSource cpds = new ComboPooledDataSource();
        cpds.setDriverClass(driver);
        cpds.setJdbcUrl(url);
        cpds.setUser(user);
        cpds.setPassword(password);
        return cpds;
    }
}
