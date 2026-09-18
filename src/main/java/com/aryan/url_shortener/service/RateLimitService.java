package com.aryan.url_shortener.service;

import java.time.Duration;

import org.springframework.dao.QueryTimeoutException;
import org.springframework.stereotype.Service;

@Service 
public class RateLimitService {
    
    private final RedisService redisService;

    public RateLimitService(RedisService redisService){
        this.redisService = redisService;
    }
    
    public boolean isAllowed(String ip){
        try {
            Long count = redisService.incrementRate(ip);

            if(count == 1){
                redisService.expireRate(ip, Duration.ofMinutes(1));
            }        

            return count <= 5;
        } catch (QueryTimeoutException e) {
            return true;
        }
    }
}