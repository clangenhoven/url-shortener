package com.clangenhoven.shortly.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateUrlRequest {

    private String url;
    private String shortUrl;

    public CreateUrlRequest() {
    }

    public CreateUrlRequest(String url) {
        this.url = url;
    }

    public CreateUrlRequest(String url, String shortUrl) {
        this.url = url;
        this.shortUrl = shortUrl;
    }

    @JsonProperty
    @NotNull
    @Pattern(regexp = "^(http|https)://\\S+\\.\\S+$")
    @Length(min = 10, max = 1024)
    public String getUrl() {
        return url;
    }

    @JsonProperty
    @Pattern(regexp = "^\\S+$")
    @Length(min = 1, max = 23)
    public String getShortUrl() {
        return shortUrl;
    }
}
