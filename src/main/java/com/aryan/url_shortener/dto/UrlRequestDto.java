package com.aryan.url_shortener.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class UrlRequestDto {
    
    @NotBlank 
    @Pattern(
        regexp = "^(https?://).+$",
        message = "Url must start with http:// or https://"
    )
    private String originalUrl;

    @Future (message = "Expiration time must be in the future")
    private LocalDateTime expiresAt;

    public UrlRequestDto() {

    }

    public UrlRequestDto(String originalUrl, LocalDateTime expiresAt) {
        this.originalUrl = originalUrl;
        this.expiresAt = expiresAt;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
}
