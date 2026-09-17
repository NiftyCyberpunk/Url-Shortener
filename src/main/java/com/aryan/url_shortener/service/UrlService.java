package com.aryan.url_shortener.service;

import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.stereotype.Service;

import com.aryan.url_shortener.dto.UrlRequestDto;
import com.aryan.url_shortener.dto.UrlResponseDto;
import com.aryan.url_shortener.entity.Url;
import com.aryan.url_shortener.exception.UrlNotFoundException;
import com.aryan.url_shortener.repository.UrlRepository;

@Service 
public class UrlService {
    private final UrlRepository urlRepository;
    private final RedisService redisService;
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private final SecureRandom secureRandom = new SecureRandom();

    public UrlService(UrlRepository urlRepository, RedisService redisService) {
        this.urlRepository = urlRepository;
        this.redisService = redisService;
    }

    private String generateShortCode() {
        StringBuilder shortCode = new StringBuilder(6);

        for(int i = 0; i < 6; i++){
            int index = secureRandom.nextInt(CHARACTERS.length());
            shortCode.append(CHARACTERS.charAt(index));
        }

        return shortCode.toString();
    }

    private boolean isDuplicateKeyViolation(DataIntegrityViolationException exception){
        Throwable cause = exception;
        while(cause != null){
            if(cause instanceof SQLException sqlException){
                if(sqlException.getErrorCode() == 1062){
                    return true;
                }
            }
            cause = cause.getCause();
        }
        return false;
    }

    private String getUrlFromDatabaseAndIncrement(String shortCode){
        Url url = urlRepository
            .findByShortCode(shortCode)
            .orElseThrow(() -> new UrlNotFoundException());
        
        urlRepository.increaseAccessCountByShortCode(shortCode);
        
        return url.getOriginalUrl();
    }

    private Url getUrlFromDatabase(String shortCode){
        Url url = urlRepository
            .findByShortCode(shortCode)
            .orElseThrow(() -> new UrlNotFoundException());

        return url;
    }

    private void increaseAccessCountInDatabase(String shortCode){
        urlRepository.increaseAccessCountByShortCode(shortCode);
    }
   
    public UrlResponseDto shortUrl(UrlRequestDto dto) {

        Optional<Url> existingUrl = urlRepository.findByOriginalUrl(dto.getOriginalUrl());

        if(existingUrl.isPresent()){
            String shortUrl = "http://nifty/" + existingUrl.get().getShortCode(); 

            redisService.setUrl(existingUrl.get().getShortCode(), existingUrl.get().getOriginalUrl());
            redisService.setCount(existingUrl.get().getShortCode(), Long.toString(existingUrl.get().getAccessCount()));

            return new UrlResponseDto(shortUrl);
        }

        String shortCode;

        while(true) {
            shortCode = generateShortCode();
            try {
                Url url =new Url(dto.getOriginalUrl(), shortCode);
                urlRepository.save(url);
                break;
            } catch (DataIntegrityViolationException e) {
                if(!isDuplicateKeyViolation(e)){
                    throw e;
                }
            }
        }   

        redisService.setUrl(shortCode, dto.getOriginalUrl());
        redisService.setCount(shortCode, "0");

        String shortUrl = "http://nifty/" + shortCode;

        UrlResponseDto responseDto = new UrlResponseDto(shortUrl);

        return responseDto;
    }

    public String getOriginalUrl(String shortCode){

        try {
            String cachedUrl = redisService.get(shortCode);

            if(cachedUrl != null){
                redisService.increment(shortCode);
                return cachedUrl;
            }
            
        } catch (QueryTimeoutException e){
            return getUrlFromDatabaseAndIncrement(shortCode);
        }
        //separating the database and redis operations so these do not clash in the catch
        Url url = getUrlFromDatabase(shortCode);

        try {
            redisService.setUrl(shortCode, url.getOriginalUrl());
            redisService.setCount(shortCode, Long.toString(url.getAccessCount()));
            redisService.increment(shortCode);
        } catch (QueryTimeoutException e) {
            increaseAccessCountInDatabase(shortCode);
        }
            return url.getOriginalUrl();
    }
}
