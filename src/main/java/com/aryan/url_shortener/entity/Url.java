package com.aryan.url_shortener.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Url {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String originalUrl;

    @Column(unique = true, nullable = false)
    private String shortCode;

    @Column(nullable = false)
    private Long accessCount;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column (nullable = false, length = 64)
    private String urlHash;

    public Url() {

    }

    public Url(String originalUrl, String shortCode, String urlHash) {
        LocalDateTime now = LocalDateTime.now();
        this.originalUrl = originalUrl;
        this.shortCode = shortCode;
        this.createdAt = now;
        this.accessCount = 0L;
        this.expiresAt = now.plusDays(7);
        this.urlHash = urlHash;
    }

    public Url(String originalUrl, String shortCode, LocalDateTime expiresAt, String urlHash) {
        this.originalUrl = originalUrl;
        this.shortCode = shortCode;
        this.createdAt = LocalDateTime.now();
        this.accessCount = 0L;
        this.expiresAt = expiresAt;
        this.urlHash = urlHash;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public String getShortCode() {
        return shortCode;
    }

    public Long getAccessCount() {
        return accessCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public String getUrlHash() {
        return urlHash;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }

    public void setAccessCount(Long accessCount) {
        this.accessCount = accessCount;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public void setUrlHash(String urlHash) {
        this.urlHash = urlHash;
    }
}
