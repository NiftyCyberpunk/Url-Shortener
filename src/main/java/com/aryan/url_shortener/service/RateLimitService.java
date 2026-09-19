package com.aryan.url_shortener.service;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

@Service
public class RateLimitService {

    private final RedisService redisService;
    private static final Logger logger = LoggerFactory.getLogger(RateLimitService.class);

    public RateLimitService(RedisService redisService) {
        this.redisService = redisService;
    }

    public boolean isAllowed(String ip) {
        try {
            Long count = redisService.incrementRate(ip);

            if (count == 1) {
                redisService.expireRate(ip, Duration.ofMinutes(1));
            }

            return count <= 5;
        } catch (DataAccessException e) {
            logger.warn("Redis unavailable. Skipping rate limiting.", e);
            return true;
        }
    }
}