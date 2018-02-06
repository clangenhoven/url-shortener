package com.clangenhoven.shortly.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

public class Url {

    private long id;
    private String url;
    private String shortUrl;
    private OffsetDateTime created;
    private long accessCount;
    private long ownerId;

    public Url() {
    }

    public Url(long id, String url, String shortUrl, OffsetDateTime created, long accessCount, long ownerId) {
        this.id = id;
        this.url = url;
        this.shortUrl = shortUrl;
        this.created = created;
        this.accessCount = accessCount;
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
    public OffsetDateTime getCreated() {
        return created;
    }

    @JsonProperty
    public long getAccessCount() {
        return accessCount;
    }

    @JsonProperty
    public long getOwnerId() {
        return ownerId;
    }
}
