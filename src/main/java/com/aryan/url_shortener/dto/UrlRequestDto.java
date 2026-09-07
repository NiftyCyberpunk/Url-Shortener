package com.aryan.url_shortener.dto;

import jakarta.validation.constraints.NotBlank;

public class UrlRequestDto {
    
    @NotBlank 
    private String originalUrl;

    public UrlRequestDto() {

    }

    public UrlRequestDto(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }
}
