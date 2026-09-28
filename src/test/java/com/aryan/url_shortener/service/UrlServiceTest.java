package com.aryan.url_shortener.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
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
import com.aryan.url_shortener.utils.HashUtil;

import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
public class UrlServiceTest {

    @Mock
    private UrlRepository urlRepository;

    @Mock
    private RedisService redisService;

    @Mock 
    private HttpServletRequest request;

    @InjectMocks
    private UrlService urlService;

    @Test
    void shouldCreateShortUrl() {
        UrlRequestDto dto = new UrlRequestDto("https://example.com", null);

        when(urlRepository.save(any(Url.class)))
            .then(invocation -> invocation.getArgument(0));

        when(request.getScheme())
            .thenReturn("https");

        when(request.getServerName())
            .thenReturn("127.0.0.1");


        UrlResponseDto responseDto = urlService.shortUrl(dto, request);

        assertNotNull(responseDto);
        assertTrue(responseDto.getShortUrl().startsWith("https://127.0.0.1/"));

        verify(urlRepository).save(any(Url.class));
        verify(redisService).setUrl(anyString(), eq("https://example.com"), any());
        verify(redisService).setCount(anyString(), eq("0"));
    }

    @Test 
    void shouldReturnExistingUrl() {

        String urlHash = HashUtil.sha256("https://example.com");
        Url existingUrl = new Url(
            "https://example.com",
            "abc123",
            urlHash
        );

        when(urlRepository.findByUrlHashAndExpiresAtAfter(eq(urlHash), any(LocalDateTime.class)))
            .thenReturn(Optional.of(existingUrl));
        
        when(request.getScheme())
            .thenReturn("https");

        when(request.getServerName())
            .thenReturn("127.0.0.1");

        UrlRequestDto dto = new UrlRequestDto("https://example.com", null);

        UrlResponseDto responseDto = urlService.shortUrl(dto, request);

        assertEquals("https://127.0.0.1/abc123", responseDto.getShortUrl());

        verify(urlRepository, never()).save(any(Url.class));
        verify(redisService).setUrl(eq("abc123"), eq("https://example.com"), any());
        verify(redisService).setCount(eq("abc123"), eq("0"));
    }
    
    @Test 
    void shouldThrowUrlExpiredException_whenExpiredUrlCalled() {

        String urlHash = HashUtil.sha256("https://example.com");
        Url expiredUrl = new Url(
            "https://example.com",
            "abc123",
            LocalDateTime.now().minusMinutes(1),
            urlHash
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

        assertThrows(UrlNotFoundException.class,
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

        String urlHash = HashUtil.sha256("https://example.com");
        Url url = new Url("https://example.com", "abc123", urlHash);

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

        String urlHash = HashUtil.sha256("https://example.com");
        Url url = new Url("https://example.com" , "abc123", urlHash);

        when(redisService.get("abc123"))
            .thenThrow(new RedisConnectionFailureException("Redis unavaliable"));
        
        when(urlRepository.findByShortCode("abc123"))
            .thenReturn(Optional.of(url));
        
        String result = urlService.getOriginalUrl("abc123");

        assertEquals("https://example.com", result);

        verify(urlRepository).findByShortCode("abc123");
        verify(urlRepository).increaseAccessCountByShortCode("abc123");
    }

    @Test 
    void shouldCreateNewUrl_whenHashMatchesButOriginalUrlIsDifferent() {

        String originalUrl = "https://google.com";
        String diffUrl = "https://youtube.com";

        String urlHash = HashUtil.sha256(originalUrl);

        Url existingUrl = new Url(diffUrl, "abc123", urlHash);

        when(urlRepository.findByUrlHashAndExpiresAtAfter(eq(urlHash), any(LocalDateTime.class)))
            .thenReturn(Optional.of(existingUrl));
        
        when(request.getScheme())
            .thenReturn("https");

        when(request.getServerName())
            .thenReturn("127.0.0.1");

        when(urlRepository.save(any(Url.class)))
            .then(invocation -> invocation.getArgument(0));
        
        UrlRequestDto dto = new UrlRequestDto(originalUrl, null);

        UrlResponseDto responseDto = urlService.shortUrl(dto, request);

        assertNotNull(responseDto);

        verify(urlRepository).save(any(Url.class));
    }

    @Test 
    void shouldStoreCorrectHash_whenCreatingNewUrl() {
        String originalUrl = "https://example.com";
        String expectedHash = HashUtil.sha256(originalUrl);

        when(request.getScheme()).thenReturn("https");
        when(request.getServerName()).thenReturn("127.0.0.1");

        when(urlRepository.save(any(Url.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        UrlRequestDto dto = new UrlRequestDto(originalUrl, null);

        urlService.shortUrl(dto, request);

        verify(urlRepository).save(argThat(url ->
            expectedHash.equals(url.getUrlHash())
        ));
    }
}
