package com.clangenhoven.shortly.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.clangenhoven.shortly.dao.UrlDao;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.lettuce.core.api.StatefulRedisConnection;
import com.clangenhoven.shortly.model.Url;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ratpack.exec.Blocking;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;

@Singleton
public class UrlService {

    private final static Logger logger = LoggerFactory.getLogger(UrlService.class);

    private ObjectMapper objectMapper = new ObjectMapper();

    private final UrlDao urlDao;
    private final StatefulRedisConnection<String, String> redisConnection;

    @Inject
    public UrlService(UrlDao urlDao, StatefulRedisConnection<String, String> redisConnection) {
        this.urlDao = urlDao;
        this.redisConnection = redisConnection;
    }

    public void lookupUrl(String shortUrl, Consumer<Optional<Url>> callback, Consumer<Throwable> errorHandler) {
        RedisReactiveCommands<String, String> reactive = redisConnection.reactive();
        reactive.get(shortUrl)
                .map(Optional::of)
                .defaultIfEmpty(Optional.empty())
                .map(o -> o.flatMap(s -> {
                    try {
                        return Optional.of(objectMapper.readValue(s, Url.class));
                    } catch (IOException e) {
                        logger.error("Failed to JSON decode Url object from string: '" + s + "'", e);
                        return Optional.empty();
                    }
                }))
                .subscribe(r -> {
                    if (r.isPresent()) {
                        logger.info("Found entry in cache for short url " + shortUrl);
                        callback.accept(r);
                    } else {
                        Blocking.get(() -> urlDao.getByShortUrl(shortUrl))
                                .then(o -> {
                                    if (o.isPresent()) {
                                        logger.info("Found entry in database for short url " + shortUrl);
                                        reactive.set(shortUrl, objectMapper.writeValueAsString(o.get()))
                                                .subscribe(logger::info, e -> logger.error("Failed to execute set", e));
                                    } else {
                                        logger.info("Did not find entry in cache or database for short url " + shortUrl);
                                    }
                                    callback.accept(o);
                                });
                    }
                }, errorHandler);
    }
}
