package com.urlshortener.common.dto;

import java.time.Instant;

public record ClickEventMessage(
        String shortKey,
        String ip,
        String userAgent,
        String referrer,
        Instant timestamp
) {}
