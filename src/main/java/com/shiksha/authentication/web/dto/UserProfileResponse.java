package com.shiksha.authentication.web.dto;

import com.shiksha.authentication.domain.UserRole;

import java.time.LocalDateTime;

public record UserProfileResponse(
        Long id,
        String email,
        String firstName,
        String lastName,
        String phone,
        UserRole role,
        Integer gradeLevel,
        LocalDateTime createdAt
) {
}