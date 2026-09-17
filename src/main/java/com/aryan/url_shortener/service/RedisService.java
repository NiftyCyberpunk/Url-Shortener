package com.aryan.url_shortener.service;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
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

    public void setUrl(String key, String value, Duration ttl){
        stringRedisTemplate.opsForValue().set(urlKey(key), value, ttl);
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

    public Long getCount(String key){
        String count = stringRedisTemplate.opsForValue().get(countKey(key));

        return Long.parseLong(count);
    }

    public Set<String> scanCountKeys(){

        HashSet<String> keySet = new HashSet<>();
        
        ScanOptions options = ScanOptions.scanOptions()
            .match("url:*:count")
            .build();
        
        Cursor<String> cursor = stringRedisTemplate.scan(options);

        try{
            while(cursor.hasNext()){
                String key = cursor.next();
                keySet.add(key);
            }
        }
        finally{
            cursor.close();
        }

        return keySet;
    }

    public void delete(String key){
        stringRedisTemplate.delete(countKey(key));
    }
}
