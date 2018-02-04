package com.clangenhoven.shortly.client;

import com.google.inject.Inject;
import io.lettuce.core.RedisClient;
import ratpack.service.Service;
import ratpack.service.StopEvent;

public class LifecycleAwareRedisClient implements Service {

    private final RedisClient redisClient;

    @Inject
    public LifecycleAwareRedisClient(RedisClient redisClient) {
        this.redisClient = redisClient;
    }

    @Override
    public void onStop(StopEvent event) throws Exception {
        redisClient.shutdown();
    }
}
