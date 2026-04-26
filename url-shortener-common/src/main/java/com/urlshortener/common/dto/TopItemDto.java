package com.urlshortener.common.dto;

public record TopItemDto(
        String name,
        long count,
        double percentage
) {}
