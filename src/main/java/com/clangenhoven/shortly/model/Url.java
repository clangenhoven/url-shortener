package com.clangenhoven.shortly.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

public class Url {

    private long id;
    private String url;
    private String shortUrl;
    private OffsetDateTime created;
    private long ownerId;

    public Url() {
    }

    public Url(long id, String url, String shortUrl, OffsetDateTime created, long ownerId) {
        this.id = id;
        this.url = url;
        this.shortUrl = shortUrl;
        this.created = created;
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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    public OffsetDateTime getCreated() {
        return created;
    }

    @JsonProperty
    public long getOwnerId() {
        return ownerId;
    }
}
