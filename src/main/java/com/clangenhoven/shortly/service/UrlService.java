package com.clangenhoven.shortly.service;

import com.clangenhoven.shortly.dao.UrlDao;
import com.clangenhoven.shortly.model.CreateUrlRequest;
import com.clangenhoven.shortly.model.Url;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import io.lettuce.core.api.sync.RedisCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ratpack.exec.Blocking;
import ratpack.exec.Promise;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@Singleton
public class UrlService {

    private final static Logger logger = LoggerFactory.getLogger(UrlService.class);

    private final ObjectMapper objectMapper;
    private final UrlDao urlDao;
    private final RedisCommands<String, String> sync;
    private final Integer cacheTtl;
    private final ShortUrlGenerator generator;

    @Inject
    public UrlService(UrlDao urlDao,
                      ObjectMapper objectMapper,
                      RedisCommands<String, String> sync,
                      @Named("cacheTtl") Integer cacheTtl,
                      ShortUrlGenerator generator) {
        this.urlDao = urlDao;
        this.objectMapper = objectMapper;
        this.sync = sync;
        this.cacheTtl = cacheTtl;
        this.generator = generator;
    }

    public Promise<Optional<String>> createUrl(CreateUrlRequest request, long ownerId) {
        return Blocking.get(() -> {
            String shortUrl = request.getShortUrl() == null ? generator.generateUrl() : request.getShortUrl();
            try {
                LocalDateTime localDateTime = OffsetDateTime.now().atZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
                urlDao.insert(request.getUrl(), shortUrl, localDateTime, ownerId);
                return Optional.of(shortUrl);
            } catch (Exception e) {
                logger.error("Caught exception while trying to create short url", e);
                return Optional.<String>empty();
            }
        });
    }

    public Promise<List<Url>> listUrls(long ownerId) {
        return Blocking.get(() -> urlDao.getByOwner(ownerId));
    }

    public Promise<Optional<Url>> lookupUrlBypassCache(String shortUrl) {
        return Blocking.get(() -> urlDao.getByShortUrl(shortUrl));
    }

    public Promise<Optional<Url>> lookupUrl(String shortUrl) {
        return Blocking.get(() -> Optional.ofNullable(sync.get(shortUrl)))
                .map(result -> result.flatMap(json -> {
                    try {
                        return Optional.of(objectMapper.readValue(json, Url.class));
                    } catch (IOException e) {
                        logger.error("Failed to JSON decode Url object from string: '" + json + "'", e);
                        return Optional.empty();
                    }
                }))
                .flatMap(maybeUrl -> {
                    if (maybeUrl.isPresent()) {
                        logger.info("Found entry in cache for short url " + shortUrl);
                        Blocking.exec(() -> urlDao.incrementUsage(maybeUrl.get().getId()));
                        return Promise.value(maybeUrl);
                    } else {
                        return Blocking.get(() -> urlDao.getByShortUrl(shortUrl))
                                .map(o -> {
                                    if (o.isPresent()) {
                                        logger.info("Found entry in database for short url " + shortUrl);
                                        Blocking.exec(() -> urlDao.incrementUsage(o.get().getId()));
                                        Blocking.exec(() ->
                                                sync.setex(shortUrl, cacheTtl, objectMapper.writeValueAsString(o.get())));
                                    } else {
                                        logger.info("Did not find entry in cache or database for short url " + shortUrl);
                                    }
                                    return o;
                                });
                    }
                });
    }
}
