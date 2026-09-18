package com.aryan.url_shortener.service;

import java.security.SecureRandom;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.stereotype.Service;

import com.aryan.url_shortener.dto.UrlRequestDto;
import com.aryan.url_shortener.dto.UrlResponseDto;
import com.aryan.url_shortener.entity.Url;
import com.aryan.url_shortener.exception.UrlExpiredException;
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
        
        if (!LocalDateTime.now().isBefore(url.getExpiresAt())) {
            throw new UrlExpiredException();
        }
        
        urlRepository.increaseAccessCountByShortCode(shortCode);
        
        return url.getOriginalUrl();
    }

    private Url getUrlFromDatabase(String shortCode){
        Url url = urlRepository
            .findByShortCode(shortCode)
            .orElseThrow(() -> new UrlNotFoundException());
            
        if(!LocalDateTime.now().isBefore(url.getExpiresAt())){
            throw new UrlExpiredException();
        }

        return url;
    }

    private void increaseAccessCountInDatabase(String shortCode){
        urlRepository.increaseAccessCountByShortCode(shortCode);
    }
   
    public UrlResponseDto shortUrl(UrlRequestDto dto) {

        Optional<Url> existingUrl = urlRepository.findByOriginalUrlAndExpiresAtAfter(dto.getOriginalUrl(), LocalDateTime.now());

        if(existingUrl.isPresent()){
            String shortUrl = "http://nifty/" + existingUrl.get().getShortCode(); 

            LocalDateTime now = LocalDateTime.now();
            Duration ttl = Duration.between(now, existingUrl.get().getExpiresAt());

            try {
                redisService.setUrl(existingUrl.get().getShortCode(), existingUrl.get().getOriginalUrl(), ttl);
                redisService.setCount(existingUrl.get().getShortCode(), Long.toString(existingUrl.get().getAccessCount()));
            } catch (QueryTimeoutException e) {
                
            }

            return new UrlResponseDto(shortUrl);
        }

        Url url;
        String shortCode;
        LocalDateTime expiresAt = dto.getExpiresAt();
        while(true) {
            shortCode = generateShortCode();
            try {
                if(expiresAt == null){
                    url =new Url(dto.getOriginalUrl(), shortCode);
                }
                else{
                    url =new Url(dto.getOriginalUrl(), shortCode, expiresAt);
                }
                urlRepository.save(url);
                break;
            } catch (DataIntegrityViolationException e) {
                if(!isDuplicateKeyViolation(e)){
                    throw e;
                }
            }
        }   

        Duration ttl = Duration.between(LocalDateTime.now(), url.getExpiresAt());

        try {
            redisService.setUrl(shortCode, dto.getOriginalUrl(), ttl);
            redisService.setCount(shortCode, "0");
        } catch (QueryTimeoutException e) {

        }

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
        Duration ttl = Duration.between(LocalDateTime.now(), url.getExpiresAt());
        try {
            redisService.setUrl(shortCode, url.getOriginalUrl(), ttl);
            redisService.setCount(shortCode, Long.toString(url.getAccessCount()));
            redisService.increment(shortCode);
        } catch (QueryTimeoutException e) {
            increaseAccessCountInDatabase(shortCode);
        }
            return url.getOriginalUrl();
    }
}
