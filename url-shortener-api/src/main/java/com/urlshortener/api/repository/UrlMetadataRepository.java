package com.urlshortener.api.repository;

import com.urlshortener.api.model.UrlMetadata;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UrlMetadataRepository extends JpaRepository<UrlMetadata, Long> {

    Optional<UrlMetadata> findByShortKey(String shortKey);

    Page<UrlMetadata> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @Query("SELECT u FROM UrlMetadata u WHERE u.user.id = :userId " +
    "AND (:search IS NULL OR LOWER(u.longUrl) LIKE LOWER(CONCAT('%',:search, '%')) " +
    "OR LOWER(u.shortKey) LIKE LOWER(CONCAT('%',:search, '%')))")
    Page<UrlMetadata> searchByUser(@Param("userId") Long userId, @Param("search") String search, Pageable pageable);

    void deleteByShortKey(String shortKey);

    @Modifying
    @Query("UPDATE UrlMetadata u SET u.clickCount = u.clickCount + 1 WHERE u.shortKey = :shortKey")
    void incrementClickCount(@Param("shortKey") String shortKey);
}
