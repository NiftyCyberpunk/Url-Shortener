package com.aryan.url_shortener.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class UrlRequestDto {
    
    @NotBlank 
    @Pattern(
        regexp = "^(https?://).+$",
        message = "Url must start with http:// or https://"
    )
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
