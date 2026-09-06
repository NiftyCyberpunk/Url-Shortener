package com.aryan.url_shortener.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aryan.url_shortener.entity.Url;

@Repository 
public interface UrlRepository extends JpaRepository<Url, Integer> {
    
    Optional<Url> findByShortCode(String shortCode);
}
