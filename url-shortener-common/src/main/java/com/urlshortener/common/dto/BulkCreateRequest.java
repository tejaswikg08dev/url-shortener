package com.urlshortener.common.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record BulkCreateRequest(
        @NotEmpty(message = "URLs list cannot be empty")
        @Size(max = 100,message = "Maximum 100 URLs per batch")
        List<CreateUrlRequest> urls
){}