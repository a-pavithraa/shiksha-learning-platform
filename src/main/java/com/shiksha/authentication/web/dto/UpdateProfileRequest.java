package com.shiksha.authentication.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @NotBlank(message = "First name is required")
        @Size(max = 100, message = "First name should not exceed 100 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 100, message = "Last name should not exceed 100 characters")
        String lastName,

        @Size(max = 20, message = "Phone number should not exceed 20 characters")
        String phone
) {
}