package com.shiksha.authentication;

import com.shiksha.authentication.domain.UserService;
import com.shiksha.authentication.domain.models.StudentDto;
import com.shiksha.authentication.domain.models.UserDto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * API class for cross-module access to user data
 * Specifically designed for notification module needs
 * This class acts as a facade to the UserService domain layer
 */
@Component
public class UserServiceAPI {

    private final UserService userService;

    public UserServiceAPI(UserService userService) {
        this.userService = userService;
    }

    /**
     * Get all students enrolled in a specific subject and grade level
     * Used by notification module to send assignment notifications
     *
     * @param subjectId the subject ID
     * @param gradeLevel the grade level (9-12)
     * @return list of students enrolled in the subject and grade
     */
    public List<StudentDto> getStudentsBySubjectAndGrade(Long subjectId, Integer gradeLevel) {
        return userService.findStudentsBySubjectAndGrade(subjectId, gradeLevel);
    }

    /**
     * Get user by ID
     * Used by academic module to access user information
     *
     * @param userId the user ID
     * @return UserDto containing user information
     */
    public Optional<UserDto> findById(Long userId) {
        return userService.findById(userId);
    }

    /**
     * Get user by username (email)
     * Used by academic module to extract user ID from authentication
     *
     * @param username the username (email)
     * @return UserDto containing user information
     */
    public UserDto findByUsername(String username) {
        return userService.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found with username: " + username));
    }
}