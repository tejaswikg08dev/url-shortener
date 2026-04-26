package com.urlshortener.common.dto;

import java.time.LocalDate;

public record DailyClickDto(
        LocalDate date,
        long clicks,
        long uniqueVisitors
) {}