package com.clangenhoven.shortly.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Url {

    private long id;
    private String url;
    private String shortUrl;
    private long ownerId;

    public Url() {
    }

    public Url(long id, String url, String shortUrl, long ownerId) {
        this.id = id;
        this.url = url;
        this.shortUrl = shortUrl;
        this.ownerId = ownerId;
    }

    @JsonProperty
    public long getId() {
        return id;
    }

    @JsonProperty
    public String getUrl() {
        return url;
    }

    @JsonProperty
    public String getShortUrl() {
        return shortUrl;
    }

    @JsonProperty
    public long getOwnerId() {
        return ownerId;
    }
}
