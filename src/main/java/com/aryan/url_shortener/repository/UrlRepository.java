package com.aryan.url_shortener.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.aryan.url_shortener.entity.Url;

@Repository
public interface UrlRepository extends JpaRepository<Url, Integer> {

    Optional<Url> findByShortCode(String shortCode);

    Optional<Url> findByOriginalUrlAndExpiresAtAfter(String originalUrl, LocalDateTime now);

    @Transactional
    @Modifying
    @Query("""
                UPDATE Url
                SET accessCount = accessCount + 1
                WHERE shortCode = :shortCode
            """)
    int increaseAccessCountByShortCode(@Param("shortCode") String shortCode);

    @Transactional
    @Modifying
    @Query("""
                   UPDATE Url
                   SET accessCount = :count
                   WHERE shortCode = :shortCode
            """)
    int updateAccessCountByShortCode(@Param("shortCode") String shortCode, @Param("count") Long count);

    List<Url> findByExpiresAtLessThanEqual(LocalDateTime now);
}
