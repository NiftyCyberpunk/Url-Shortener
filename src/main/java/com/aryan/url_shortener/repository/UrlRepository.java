package com.aryan.url_shortener.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aryan.url_shortener.entity.Url;

@Repository 
public interface UrlRepository extends JpaRepository<Url, Integer> {
    
    Optional<Url> findByShortCode(String shortCode);

    Optional<Url> findByOriginalUrl(String originalUrl);

    @Modifying 
    @Query ("""
        UPDATE Url
        SET accessCount = accessCount + 1
        WHERE shortCode = :shortCode
    """)
    int increaseAccessCountByShortCode(@Param("shortCode") String shortCode);
}
