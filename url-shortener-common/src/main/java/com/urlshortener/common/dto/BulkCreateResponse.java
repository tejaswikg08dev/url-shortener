package com.urlshortener.common.dto;

import java.util.List;

public record BulkCreateResponse(
        List<UrlResponse> successful,
        List<BulkError> failed,
        int totalRequested,
        int totalSuccessful,
        int totalFailed
) {}