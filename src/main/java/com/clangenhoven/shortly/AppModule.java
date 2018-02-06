package com.clangenhoven.shortly;

import com.clangenhoven.shortly.auth.Authenticator;
import com.clangenhoven.shortly.dao.UrlDao;
import com.clangenhoven.shortly.dao.UserDao;
import com.clangenhoven.shortly.handler.UrlCreator;
import com.clangenhoven.shortly.handler.UrlHandler;
import com.clangenhoven.shortly.handler.UrlLister;
import com.clangenhoven.shortly.handler.UrlRedirector;
import com.clangenhoven.shortly.handler.UserCreator;
import com.clangenhoven.shortly.model.Url;
import com.clangenhoven.shortly.model.User;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.netty.buffer.ByteBuf;
import io.netty.util.AsciiString;
import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.apache.shiro.authc.credential.PasswordService;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.pac4j.http.client.direct.DirectBasicAuthClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ratpack.error.ClientErrorHandler;
import ratpack.error.ServerErrorHandler;
import ratpack.exec.Operation;
import ratpack.exec.Promise;
import ratpack.http.MediaType;
import ratpack.session.SessionStore;

import javax.sql.DataSource;
import javax.validation.Validation;
import javax.validation.Validator;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.TimeZone;

import static ratpack.groovy.Groovy.groovyTemplate;

public class AppModule extends AbstractModule {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppModule.class);
    private static final Calendar UTC_CALENDAR = Calendar.getInstance(TimeZone.getTimeZone(ZoneOffset.UTC));

    @Override
    protected void configure() {
        bind(SessionStore.class).to(NoOpSessionStore.class);
        bind(Authenticator.class);
        bind(PasswordService.class).to(DefaultPasswordService.class);
        bind(Validator.class).toInstance(Validation.buildDefaultValidatorFactory().getValidator());
        bind(UserCreator.class);
        bind(UrlHandler.class);
        bind(UrlRedirector.class);
        bind(UrlCreator.class);
        bind(UrlLister.class);
        bind(ClientErrorHandler.class).toInstance((ctx, statusCode) -> {
            ctx.getResponse().status(statusCode);
            if (MediaType.APPLICATION_JSON.equals(ctx.getRequest().getContentType().getType())) {
                ctx.getResponse().send();
            } else {
                if (statusCode == 404) {
                    ctx.render(groovyTemplate("error404.html"));
                } else if (statusCode == 401) {
                    ctx.render(groovyTemplate("error401.html"));
                } else if (statusCode == 403) {
                    ctx.render(groovyTemplate("error403.html"));
                }
            }
            LOGGER.error("Client Error Handler returning status code " + statusCode);
        });
        bind(ServerErrorHandler.class).toInstance((ctx, error) -> {
            LOGGER.error("Server Error Handler caught exception", error);
            ctx.render(groovyTemplate("error500.html"));
        });
    }

    @Provides
    @Singleton
    private DirectBasicAuthClient directBasicAuthClient(Authenticator authenticator) {
        return new DirectBasicAuthClient(authenticator);
    }

    @Provides
    @Singleton
    private ObjectMapper objectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
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
    private UserDao userDao(Jdbi jdbi) {
        return jdbi.onDemand(UserDao.class);
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
                        OffsetDateTime.ofInstant(rs.getTimestamp("created", UTC_CALENDAR).toInstant(), ZoneOffset.UTC),
                        rs.getLong("access_count"),
                        rs.getLong("owner_id")));
        jdbi.registerRowMapper(User.class, (rs, ctx) ->
                new User(rs.getLong("id"),
                        rs.getString("username"),
                        rs.getString("hashed_password")));
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
