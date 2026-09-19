package com.aryan.url_shortener.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public class UrlResponseDto {
    
    @NotBlank 
    @Schema (
        description = "The shorten URL",
        example = "http://nifty/{shortcode}"
    )
    private String shortUrl;

    public UrlResponseDto() {

    }

    public UrlResponseDto(String shortUrl) {
        this.shortUrl = shortUrl;
    }

    public String getShortUrl() {
        return shortUrl;
    }

    public void setShortUrl(String shortUrl) {
        this.shortUrl = shortUrl;
    }
}
