package com.urlshortener.common.dto;

import java.util.List;
import java.time.Instant;

public record UrlStatsResponse(
        String shortKey,
        long totalClicks,
        long uniqueVisitors,
        Instant createdAt,
        List<DailyClickDto> clicksByDay,
        List<TopItemDto> topCountries,
        List<TopItemDto> topReferrers,
        List<TopItemDto> deviceBreakdown
) {}