package com.urlshortener.common.dto;

import java.time.Instant;

public record ApiError(
        int status,
        String error,
        String message,
        Instant timestamp,
        String path,
        String requestId
) {}
