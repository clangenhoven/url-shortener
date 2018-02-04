package com.clangenhoven.shortly.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.clangenhoven.shortly.dao.UrlDao;
import io.lettuce.core.api.StatefulRedisConnection;
import com.clangenhoven.shortly.model.Url;
import ratpack.exec.Blocking;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;

public class UrlService {

    private ObjectMapper objectMapper = new ObjectMapper();

    private final UrlDao urlDao;
    private final StatefulRedisConnection<String, String> redisConnection;

    public UrlService(UrlDao urlDao, StatefulRedisConnection<String, String> redisConnection) {
        this.urlDao = urlDao;
        this.redisConnection = redisConnection;
    }

    public void lookupUrl(String shortUrl, Consumer<Optional<Url>> callback, Consumer<Throwable> errorHandler) {
        redisConnection.reactive().get(shortUrl)
                .map(Optional::of)
                .defaultIfEmpty(Optional.empty())
                .map(o -> o.flatMap(s -> {
                    try {
                        return Optional.of(objectMapper.readValue(s, Url.class));
                    } catch (IOException e) {
                        // todo-chris: log exception
                        return Optional.empty();
                    }
                }))
                .subscribe(r -> {
                    if (r.isPresent()) {
                        callback.accept(r);
                    } else {
                        Blocking.get(() -> urlDao.getByShortUrl(shortUrl))
                                .then(callback::accept);
                    }
                }, errorHandler);
    }
}
