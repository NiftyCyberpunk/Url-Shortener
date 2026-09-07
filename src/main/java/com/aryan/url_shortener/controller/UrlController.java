package com.aryan.url_shortener.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aryan.url_shortener.dto.UrlRequestDto;
import com.aryan.url_shortener.dto.UrlResponseDto;
import com.aryan.url_shortener.service.UrlService;

import jakarta.validation.Valid;

@RestController  
@RequestMapping ("/api/url-shortener/")
public class UrlController {
    
    private final UrlService urlService;

    public UrlController(UrlService urlService) {
        this.urlService = urlService;
    }

    @PostMapping ("shortenUrl")
    public UrlResponseDto shortUrl(@Valid @RequestBody UrlRequestDto dto) {
        return urlService.shortUrl(dto);
    }
}
