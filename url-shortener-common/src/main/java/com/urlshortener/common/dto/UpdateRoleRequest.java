package com.urlshortener.common.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateRoleRequest(
        @NotBlank
        String role
) {}
