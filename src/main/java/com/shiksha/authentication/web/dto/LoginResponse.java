package com.shiksha.authentication.web.dto;

import com.shiksha.authentication.domain.UserRole;

public record LoginResponse(
        String token,
        String refreshToken,
        int expiresIn,
        UserResponse user
) {
    public record UserResponse(
            Long id,
            String email,
            String firstName,
            String lastName,
            UserRole role
    ) {}
}