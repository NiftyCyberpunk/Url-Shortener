package com.aryan.url_shortener.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;

@ExtendWith (MockitoExtension.class)
public class RateLimitServiceTest {
    
    @Mock 
    private RedisService redisService;

    @InjectMocks 
    private RateLimitService rateLimitService;

    @Test
    void shouldAllowFirstRequestAndSetExpiry() {

        when(redisService.incrementRate("127.0.0.1"))
                .thenReturn(1L);

        boolean result = rateLimitService.isAllowed("127.0.0.1");

        assertTrue(result);

        verify(redisService).incrementRate("127.0.0.1");

        verify(redisService).expireRate(
                eq("127.0.0.1"),
                eq(Duration.ofMinutes(1))
        );
    }

    @Test
    void shouldAllowRequestsUpToLimit() {

        when(redisService.incrementRate("127.0.0.1"))
                .thenReturn(2L, 3L, 4L, 5L);

        assertTrue(rateLimitService.isAllowed("127.0.0.1"));
        assertTrue(rateLimitService.isAllowed("127.0.0.1"));
        assertTrue(rateLimitService.isAllowed("127.0.0.1"));
        assertTrue(rateLimitService.isAllowed("127.0.0.1"));

        verify(redisService, times(4))
                .incrementRate("127.0.0.1");

        verify(redisService, never())
                .expireRate(anyString(), any());
    }

    @Test
    void shouldRejectRequestAfterLimitAndThrowRateLimitExceededException() {

        when(redisService.incrementRate("127.0.0.1"))
                .thenReturn(6L);

        boolean result = rateLimitService.isAllowed("127.0.0.1");

        assertFalse(result);

        verify(redisService).incrementRate("127.0.0.1");

        verify(redisService, never())
                .expireRate(anyString(), any());
    }
    
    @Test
    void shouldAllowRequestWhenRedisFails() {

        when(redisService.incrementRate("127.0.0.1"))
                .thenThrow(new RedisConnectionFailureException("Redis unavailable"));

        boolean result = rateLimitService.isAllowed("127.0.0.1");

        assertTrue(result);

        verify(redisService).incrementRate("127.0.0.1");
    }
}
