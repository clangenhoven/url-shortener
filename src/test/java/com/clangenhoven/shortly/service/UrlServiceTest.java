package com.clangenhoven.shortly.service;

import com.clangenhoven.shortly.dao.UrlDao;
import com.clangenhoven.shortly.model.CreateUrlRequest;
import com.clangenhoven.shortly.model.Url;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.api.sync.RedisCommands;
import org.junit.Before;
import org.junit.Test;
import ratpack.exec.ExecResult;
import ratpack.test.exec.ExecHarness;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UrlServiceTest {

    private UrlDao urlDao;
    private ObjectMapper objectMapper;
    private RedisCommands<String, String> sync;
    private ShortUrlGenerator generator;
    private UrlService service;
    private final Integer cacheTtl = 10;

    @Before
    @SuppressWarnings("unchecked")
    public void setup() {
        urlDao = mock(UrlDao.class);
        objectMapper = mock(ObjectMapper.class);
        sync = (RedisCommands<String, String>) mock(RedisCommands.class);
        generator = mock(ShortUrlGenerator.class);
        service = new UrlService(urlDao, objectMapper, sync, cacheTtl, generator);
    }

    @Test
    public void delegatesToGeneratorWhenNoShortUrlProvided() throws Exception {
        when(generator.generateUrl()).thenReturn("short");
        try (ExecHarness harness = ExecHarness.harness()) {
            ExecResult<Optional<String>> shortUrl = harness.yield(execution ->
                    service.createUrl(new CreateUrlRequest("http://www.example.com"), 1));
            assertEquals("short", shortUrl.getValue().orElse(null));
        }
        verify(generator, times(1)).generateUrl();
        verify(urlDao, times(1))
                .insert(eq("http://www.example.com"), eq("short"), any(LocalDateTime.class), eq(1L));
    }

    @Test
    public void returnsEmptyIfInsertThrows() throws Exception {
        when(generator.generateUrl()).thenReturn("short");
        doThrow(new RuntimeException()).when(urlDao)
                .insert(eq("http://www.example.com"), eq("short"), any(LocalDateTime.class), eq(1L));
        try (ExecHarness harness = ExecHarness.harness()) {
            ExecResult<Optional<String>> result = harness.yield(execution ->
                    service.createUrl(new CreateUrlRequest("http://www.example.com"), 1));
            assertEquals(Optional.empty(), result.getValue());
        }
        verify(generator, times(1)).generateUrl();
    }

    @Test
    public void delegatesToCachedUrlIfPresentAndIncrementsUsage() throws Exception {
        String json = "json encoded url object";
        Url url = mock(Url.class);
        when(sync.get("short")).thenReturn(json);
        when(objectMapper.readValue(json, Url.class)).thenReturn(url);
        when(url.getId()).thenReturn(5L);
        try (ExecHarness harness = ExecHarness.harness()) {
            ExecResult<Optional<Url>> result = harness.yield(execution -> service.lookupUrl("short"));
            assertEquals(5L, result.getValue().get().getId());
        }
        verify(urlDao, times(1)).incrementUsage(5L);
    }

    @Test
    public void delegatesToUrlDaoIfNotCachedAndIncrementsUsage() throws Exception {
        String json = "json encoded url object";
        Url url = mock(Url.class);
        when(sync.get("short")).thenReturn(null);
        when(urlDao.getByShortUrl("short")).thenReturn(Optional.of(url));
        when(url.getId()).thenReturn(5L);
        when(objectMapper.writeValueAsString(url)).thenReturn(json);
        try (ExecHarness harness = ExecHarness.harness()) {
            ExecResult<Optional<Url>> result = harness.yield(execution -> service.lookupUrl("short"));
            assertEquals(5L, result.getValue().get().getId());
        }
        verify(sync, times(1)).setex("short", cacheTtl, json);
        verify(urlDao, times(1)).incrementUsage(5L);
    }
}