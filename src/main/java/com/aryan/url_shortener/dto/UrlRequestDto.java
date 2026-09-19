package com.aryan.url_shortener.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UrlRequestDto {
    
    @NotBlank 
    @Size (max = 2048, message = "URL too long. Must not exceed 2048 characters.")
    @Pattern(
        regexp = "^(https?://).+$",
        message = "Url must start with http:// or https://"
    )
    @Schema (
        description = "The original URL to shorten",
        example = "https://www.example.com"
    )
    private String originalUrl;

    @Future (message = "Expiration time must be in the future")
    @Schema(
        description = "Optional expiration time. If omitted, the URL expires after 7 days.",
        example = "2026-09-25T19:00:00"
    )
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
