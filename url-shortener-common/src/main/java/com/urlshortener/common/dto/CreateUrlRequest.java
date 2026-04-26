package com.urlshortener.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;

public record CreateUrlRequest(

        @NotBlank(message = "Long URL is required")
        @Size(max=2048, message = "URL must be under 2048 characters")
        String longUrl,

        @Size(min = 3, max = 20, message = "Alias must be 3-20 characters")
        @Pattern(regexp = "^[a-zA-Z0-9\\-_]+$", message = "Alias can only contain letters, numbers, hyphens, underscores")
        String customAlias,

        Instant expiresAt,

        List<String> tags

){}