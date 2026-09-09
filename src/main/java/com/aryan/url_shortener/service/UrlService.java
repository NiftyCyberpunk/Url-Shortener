package com.aryan.url_shortener.service;

import java.security.SecureRandom;

import org.springframework.stereotype.Service;

import com.aryan.url_shortener.dto.UrlRequestDto;
import com.aryan.url_shortener.dto.UrlResponseDto;
import com.aryan.url_shortener.entity.Url;
import com.aryan.url_shortener.exception.UrlNotFoundException;
import com.aryan.url_shortener.repository.UrlRepository;

import jakarta.transaction.Transactional;

@Service 
public class UrlService {
    private UrlRepository urlRepository;
    private static final String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    private final SecureRandom secureRandom = new SecureRandom();

    public UrlService(UrlRepository urlRepository) {
        this.urlRepository = urlRepository;
    }

    private String generateShortCode() {
        StringBuilder shortCode = new StringBuilder(6);

        for(int i = 0; i < 6; i++){
            int index = secureRandom.nextInt(characters.length());
            shortCode.append(characters.charAt(index));
        }

        return shortCode.toString();
    }

    @Transactional 
    public UrlResponseDto shortUrl(UrlRequestDto dto) {

        String shortCode;

        while(true) {
            shortCode = generateShortCode();

            if(urlRepository.findByShortCode(shortCode).isEmpty()) {
                break;
            }
        }

        Url url =new Url(dto.getOriginalUrl(), shortCode);

        urlRepository.save(url);

        String shortUrl = "http://nifty/" + shortCode;

        UrlResponseDto responseDto = new UrlResponseDto(shortUrl);

        return responseDto;
    }

    @Transactional 
    public String getOriginalUrl(String shortCode){

        Url url = urlRepository
            .findByShortCode(shortCode)
            .orElseThrow(() -> new UrlNotFoundException());
        
        url.setAccessCount(url.getAccessCount() + 1);

        urlRepository.save(url);

        return url.getOriginalUrl();
    }
}
