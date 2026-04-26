package com.urlshortener.commons.dto;

import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;

public record UpdateUrlRequest(

        @Size(max = 2048)
        String longUrl,

        Instant expiresAt,

        List<String> tags
){}