package com.aryan.url_shortener.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;

import com.aryan.url_shortener.dto.UrlRequestDto;
import com.aryan.url_shortener.dto.UrlResponseDto;
import com.aryan.url_shortener.entity.Url;
import com.aryan.url_shortener.exception.UrlExpiredException;
import com.aryan.url_shortener.exception.UrlNotFoundException;
import com.aryan.url_shortener.repository.UrlRepository;

@ExtendWith(MockitoExtension.class)
public class UrlServiceTest {

    @Mock
    private UrlRepository urlRepository;

    @Mock
    private RedisService redisService;

    @InjectMocks
    private UrlService urlService;

    @Test
    void shouldCreateShortUrl() {
        UrlRequestDto dto = new UrlRequestDto("https://example.com", null);

        when(urlRepository.save(any(Url.class)))
            .then(invocation -> invocation.getArgument(0));

        UrlResponseDto responseDto = urlService.shortUrl(dto);

        assertNotNull(responseDto);
        assertTrue(responseDto.getShortUrl().startsWith("http://nifty/"));

        verify(urlRepository).save(any(Url.class));
        verify(redisService).setUrl(anyString(), eq("https://example.com"), any());
        verify(redisService).setCount(anyString(), eq("0"));
    }

    @Test 
    void shouldReturnExistingUrl() {
        Url existingUrl = new Url(
            "https://example.com",
            "abc123"
        );

        when(urlRepository.findByOriginalUrlAndExpiresAtAfter(eq("https://example.com"), any(LocalDateTime.class)))
            .thenReturn(Optional.of(existingUrl));

        UrlRequestDto dto = new UrlRequestDto("https://example.com", null);

        UrlResponseDto responseDto = urlService.shortUrl(dto);

        assertEquals("http://nifty/abc123", responseDto.getShortUrl());

        verify(urlRepository, never()).save(any(Url.class));
        verify(redisService).setUrl(eq("abc123"), eq("https://example.com"), any());
        verify(redisService).setCount(eq("abc123"), eq("0"));
    }
    
    @Test 
    void shouldThrowUrlExpiredException_whenExpiredUrlCalled() {
        Url expiredUrl = new Url(
            "https://example.com",
            "abc123",
            LocalDateTime.now().minusMinutes(1)
        );

        when(urlRepository.findByShortCode("abc123"))
            .thenReturn(Optional.of(expiredUrl));

        assertThrows(
            UrlExpiredException.class, 
            () -> urlService.getOriginalUrl("abc123")
        );

        verify(urlRepository, never()).increaseAccessCountByShortCode(anyString());
    }

    @Test
    void shouldThrowExceptionForUnknownShortCode() {

        when(urlRepository.findByShortCode("XYZ999"))
                .thenReturn(Optional.empty());

        assertThrows(
                UrlNotFoundException.class,
                () -> urlService.getOriginalUrl("XYZ999")
        );

        verify(urlRepository, never())
                .increaseAccessCountByShortCode(anyString());
    }

    @Test 
    void shouldReturnUrlFromRedis() {
        
        when(redisService.get("abc123"))
            .thenReturn("https://example.com");
        

        String result = urlService.getOriginalUrl("abc123");

        assertEquals("https://example.com", result);

        verify(redisService).get("abc123");
        verify(redisService).increment("abc123");
        verifyNoInteractions(urlRepository);
    }

    @Test 
    void shouldGetDataFromMySqlAndBuildRedisCache() {

        Url url = new Url("https://example.com","abc123");

        when(redisService.get("abc123"))
            .thenReturn(null);

        when(urlRepository.findByShortCode("abc123"))
            .thenReturn(Optional.of(url));
        
        String result = urlService.getOriginalUrl("abc123");

        assertEquals("https://example.com", result);

        verify(urlRepository).findByShortCode("abc123");
        verify(redisService).setUrl(eq("abc123"), eq("https://example.com"), any());
        verify(redisService).setCount(eq("abc123"), eq("0"));
        verify(redisService).increment("abc123");
    }

    @Test 
    void shouldFallbackToMySql_whenRedisNotAvaliable() {
        Url url = new Url("https://example.com","abc123");

        when(redisService.get("abc123"))
            .thenThrow(new RedisConnectionFailureException("Redis unavaliable"));
        
        when(urlRepository.findByShortCode("abc123"))
            .thenReturn(Optional.of(url));
        
        String result = urlService.getOriginalUrl("abc123");

        assertEquals("https://example.com", result);

        verify(urlRepository).findByShortCode("abc123");
        verify(urlRepository).increaseAccessCountByShortCode("abc123");
    }
}
