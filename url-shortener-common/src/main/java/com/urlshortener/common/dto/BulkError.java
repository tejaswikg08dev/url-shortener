package com.urlshortener.common.dto;

public record BulkError(
        int index,
        String longUrl,
        String error
) {}