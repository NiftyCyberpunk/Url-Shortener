package com.aryan.url_shortener.service;

import java.util.Set;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.aryan.url_shortener.repository.UrlRepository;

import io.lettuce.core.RedisException;
import jakarta.transaction.Transactional;

@Service 
public class UrlSyncService {
    
    private final UrlRepository urlRepository;
    private final RedisService redisService;


    public UrlSyncService(UrlRepository urlRepository, RedisService redisService){
        this.urlRepository = urlRepository;
        this.redisService = redisService;
    } 

    @Scheduled (fixedRate = 300000)
    @Transactional 
    public void syncCount(){
        try {
            Set<String> countKeys = redisService.scanCountKeys();

            for(String key : countKeys){
                int len = key.length();
                String shortCode = key.substring(4, len - 6);
                Long count = redisService.getCount(shortCode);

                urlRepository.updateAccessCountByShortCode(shortCode, count);
            }
        } catch (RedisException e) {
             System.out.println("Redis unavailable. Sync failed.");
        }
    }
}
