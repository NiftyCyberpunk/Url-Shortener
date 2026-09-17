package com.aryan.url_shortener.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.aryan.url_shortener.entity.Url;
import com.aryan.url_shortener.repository.UrlRepository;

import io.lettuce.core.RedisException;
import jakarta.transaction.Transactional;

@Service 
public class DatabaseMaintenanceService {
    
    private final UrlRepository urlRepository;
    private final RedisService redisService;


    public DatabaseMaintenanceService(UrlRepository urlRepository, RedisService redisService){
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

    @Scheduled (fixedRate = 86400000)
    @Transactional 
    public void expiredUrlCleanUp(){
        try {
            List<Url> expiredUrl = urlRepository.findByExpiresAtLessThanEqual(LocalDateTime.now());
            
            for(Url url:expiredUrl){

                Long count = redisService.getCount(url.getShortCode());
                
                if(count != null){
                    urlRepository.updateAccessCountByShortCode(url.getShortCode(), count);
                }

                redisService.delete(url.getShortCode());
                urlRepository.delete(url);
            }
        } catch (RedisException e) {
            System.out.println("Redis Unavaliable... Skipping cleanup.");
        }
    }
}
