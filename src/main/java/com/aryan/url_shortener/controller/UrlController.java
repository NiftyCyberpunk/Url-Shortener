package com.aryan.url_shortener.controller;

import java.net.URI;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.aryan.url_shortener.dto.UrlRequestDto;
import com.aryan.url_shortener.dto.UrlResponseDto;
import com.aryan.url_shortener.service.UrlService;

import jakarta.validation.Valid;

@RestController  
public class UrlController {
    
    private final UrlService urlService;

    public UrlController(UrlService urlService) {
        this.urlService = urlService;
    }

    @PostMapping ("/api/url-shortener")
    public UrlResponseDto shortUrl(@Valid @RequestBody UrlRequestDto dto) {
        return urlService.shortUrl(dto);
    }

    @GetMapping ("/nifty/{shortCode}")

    public ResponseEntity<Void> getOriginalUrl(@PathVariable String shortCode){
        String originalUrl = urlService.getOriginalUrl(shortCode);

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(originalUrl));

        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }
}
