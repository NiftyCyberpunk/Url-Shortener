package com.aryan.url_shortener.dto;

import jakarta.validation.constraints.NotBlank;

public class UrlResponseDto {
    
    @NotBlank 
    private String shortUrl;

    public String getShortUrl() {
        return shortUrl;
    }

    public void setShortUrl(String shortUrl) {
        this.shortUrl = shortUrl;
    }
}
