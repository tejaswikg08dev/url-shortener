package com.urlshortener.common.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 100, message = "Password must be 8-100 Characters")
        String password,

        @NotBlank(message = "Name is required")
        @Size(max =100, message = "Name must be under 100 Characters")
        String name
) { }