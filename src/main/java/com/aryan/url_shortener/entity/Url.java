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
    private Integer id;

    @Column (nullable = false)
    private String originalUrl;
    
    @Column (unique = true, nullable = false)
    private String shortCode;

    @Column (nullable = false)
    private Integer accessCount;

    @Column (nullable = false)
    private LocalDateTime createdAt;

    public String getOriginalUrl() {
        return originalUrl;
    }

    public String getShortCode() {
        return shortCode;
    }

    public Integer getAccessCount() {
        return accessCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }

    public void setAccessCount(Integer accessCount) {
        this.accessCount = accessCount;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
