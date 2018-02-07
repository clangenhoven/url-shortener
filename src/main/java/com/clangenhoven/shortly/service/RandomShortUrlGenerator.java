package com.clangenhoven.shortly.service;

import com.google.inject.Singleton;

import java.util.Date;
import java.util.Random;

@Singleton
public class RandomShortUrlGenerator implements ShortUrlGenerator {

    private final Random rand;

    public RandomShortUrlGenerator() {
        this.rand = new Random(new Date().getTime());
    }

    @Override
    public String generateUrl() {
        return Long.toHexString(rand.nextLong());
    }
}
