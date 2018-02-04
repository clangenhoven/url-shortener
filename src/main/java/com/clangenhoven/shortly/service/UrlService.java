package com.clangenhoven.shortly.service;

import com.clangenhoven.shortly.dao.UrlDao;
import com.clangenhoven.shortly.model.CreateUrlRequest;
import com.clangenhoven.shortly.model.Url;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ratpack.exec.Blocking;

import java.io.IOException;
import java.util.Date;
import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;

@Singleton
public class UrlService {

    private final static Logger logger = LoggerFactory.getLogger(UrlService.class);

    private ObjectMapper objectMapper = new ObjectMapper();

    private final UrlDao urlDao;
    private final StatefulRedisConnection<String, String> redisConnection;
    private final Integer cacheTtl;
    private final Random rand;

    @Inject
    public UrlService(UrlDao urlDao,
                      StatefulRedisConnection<String, String> redisConnection,
                      @Named("cacheTtl") Integer cacheTtl) {
        this.urlDao = urlDao;
        this.redisConnection = redisConnection;
        this.cacheTtl = cacheTtl;
        this.rand = new Random(new Date().getTime());
    }

    public void createUrl(CreateUrlRequest request, long ownerId, Consumer<Optional<String>> callback) {
        Blocking.get(() -> {
            String shortUrl = request.getShortUrl() == null ? Long.toHexString(rand.nextLong()) : request.getShortUrl();
            try {
                urlDao.insert(request.getUrl(), shortUrl, ownerId);
                return Optional.of(shortUrl);
            } catch (Exception e) {
                logger.error("Caught exception while trying to create short url", e);
                return Optional.<String>empty();
            }
        }).then(callback::accept);
    }

    public void lookupUrl(String shortUrl, Consumer<Optional<Url>> callback) {
        RedisCommands<String, String> sync = redisConnection.sync();
        Blocking.get(() -> Optional.ofNullable(sync.get(shortUrl)))
                .then(result -> {
                    Optional<Url> maybeUrl = result.flatMap(s -> {
                        try {
                            return Optional.of(objectMapper.readValue(s, Url.class));
                        } catch (IOException e) {
                            logger.error("Failed to JSON decode Url object from string: '" + s + "'", e);
                            return Optional.empty();
                        }
                    });
                    if (maybeUrl.isPresent()) {
                        logger.info("Found entry in cache for short url " + shortUrl);
                        callback.accept(maybeUrl);
                    } else {
                        Blocking.get(() -> urlDao.getByShortUrl(shortUrl))
                                .then(o -> {
                                    if (o.isPresent()) {
                                        logger.info("Found entry in database for short url " + shortUrl);
                                        Blocking.exec(() -> {
                                            sync.setex(shortUrl, cacheTtl, objectMapper.writeValueAsString(o.get()));
                                        });
                                    } else {
                                        logger.info("Did not find entry in cache or database for short url " + shortUrl);
                                    }
                                    callback.accept(o);
                                });
                    }
                });
    }
}
