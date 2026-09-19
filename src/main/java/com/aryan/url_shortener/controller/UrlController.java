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
import com.aryan.url_shortener.exception.RateLimitExceededException;
import com.aryan.url_shortener.service.UrlService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import com.aryan.url_shortener.service.RateLimitService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
public class UrlController {

    private final UrlService urlService;
    private final RateLimitService rateLimitService;

    public UrlController(UrlService urlService, RateLimitService rateLimitService) {
        this.urlService = urlService;
        this.rateLimitService = rateLimitService;
    }

    @Operation(
        summary = "Create a short URL",
        description = "Creates a shortened URL with an optional expiration time."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Short URL created"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "429", description = "Rate limit exceeded"),
        @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/api/url-shortener")
    public ResponseEntity<UrlResponseDto> shortUrl(@Valid @RequestBody UrlRequestDto dto, HttpServletRequest request) {

        String ip = request.getRemoteAddr();

        if (!rateLimitService.isAllowed(ip)) {
            throw new RateLimitExceededException();
        }

        UrlResponseDto responseDto = urlService.shortUrl(dto);

        URI location = URI.create(responseDto.getShortUrl());

        return ResponseEntity
                .created(location)
                .body(responseDto);
    }

    @Operation (
        summary = "Redirects to the original URL",
        description = "Redirects the client to the original URL associated with the short code."
    )
    @ApiResponses ({
        @ApiResponse(responseCode = "302", description = "Redirect successful"),
        @ApiResponse(responseCode = "404", description = "Short URL not found"),
        @ApiResponse(responseCode = "410", description = "Short URL has expired"),
        @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @GetMapping("/nifty/{shortCode}")

    public ResponseEntity<Void> getOriginalUrl(@PathVariable String shortCode) {
        String originalUrl = urlService.getOriginalUrl(shortCode);

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(originalUrl));

        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }
}
