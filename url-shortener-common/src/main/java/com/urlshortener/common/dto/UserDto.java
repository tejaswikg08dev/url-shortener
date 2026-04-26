package com.urlshortener.common.dto;

import java.time.Instant;

public record UserDto(
        Long id,
        String email,
        String name,
        String role,
        Instant createdAt
) {}