package com.clangenhoven.shortly.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateUrlRequest {

    private String url;
    private String shortUrl;

    public CreateUrlRequest() {
    }

    public CreateUrlRequest(String url, String shortUrl) {
        this.url = url;
        this.shortUrl = shortUrl;
    }

    @JsonProperty(required = true)
    public String getUrl() {
        return url;
    }

    @JsonProperty
    public String getShortUrl() {
        return shortUrl;
    }
}
