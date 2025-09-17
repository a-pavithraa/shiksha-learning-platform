package com.shiksha.authentication.web.dto;

import com.shiksha.authentication.domain.UserRole;
import java.util.List;

public record RegisterCommand(
        String email,
        String password,
        String firstName,
        String lastName,
        String phone,
        UserRole role,
        Integer gradeLevel,
        List<Long> subjectIds
) {
    public static RegisterCommand from(RegisterRequest request) {
        return new RegisterCommand(
                request.email(),
                request.password(),
                request.firstName(),
                request.lastName(),
                request.phone(),
                request.role(),
                request.gradeLevel(),
                request.subjectIds()
        );
    }
}