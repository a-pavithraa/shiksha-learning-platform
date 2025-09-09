package com.shiksha.authentication.web.dto;

import com.shiksha.authentication.domain.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password should have at least 8 characters")
        String password,

        @NotBlank(message = "First name is required")
        @Size(max = 100, message = "First name should not exceed 100 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 100, message = "Last name should not exceed 100 characters")
        String lastName,

        @Size(max = 20, message = "Phone number should not exceed 20 characters")
        String phone,

        @NotNull(message = "Role is required")
        UserRole role,

        Integer gradeLevel
) {
}