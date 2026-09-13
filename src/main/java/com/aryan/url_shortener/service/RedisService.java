package com.aryan.url_shortener.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service 
public class RedisService {
    
    private final StringRedisTemplate stringRedisTemplate;

    private String urlKey(String key){
        return "url:" + key;
    }

    private String countKey(String key){
        return "url:" + key + ":count";
    }

    public RedisService(StringRedisTemplate stringRedisTemplate){
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public void setUrl(String key, String value){
        stringRedisTemplate.opsForValue().set(urlKey(key), value);
    }

    public void setCount(String key, String value){
        stringRedisTemplate.opsForValue().set(countKey(key), value);
    }

    public String get(String key){
        return stringRedisTemplate.opsForValue().get(urlKey(key));
    }

    public Long increment(String key){
        return stringRedisTemplate.opsForValue().increment(countKey(key));
    }
}
