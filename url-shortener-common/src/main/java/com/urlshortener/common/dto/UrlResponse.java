package com.urlshortener.common.dto;

import java.time.Instant;
import java.util.List;

public record UrlResponse(
        String shortKey,
        String shortUrl,
        String longUrl,
        boolean customAlias,
        Instant expiresAt,
        List<String> tags,
        long clickCount,
        Instant createdAt
) {}