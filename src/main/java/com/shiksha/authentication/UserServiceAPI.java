package com.shiksha.authentication;

import com.shiksha.authentication.domain.User;
import com.shiksha.authentication.domain.UserService;
import com.shiksha.authentication.domain.models.StudentDto;
import org.springframework.stereotype.Component;

import java.util.List;

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
     * Get user's email for sending notifications
     * 
     * @param userId the user ID
     * @return user's email address, or null if user not found
     */
    public String getUserEmail(Long userId) {
        return userService.findById(userId)
                .map(User::getEmail)
                .orElse(null);
    }

    /**
     * Get user's full name for personalized notifications
     * 
     * @param userId the user ID
     * @return user's full name, or "Student" if user not found
     */
    public String getUserFullName(Long userId) {
        return userService.findById(userId)
                .map(User::getFullName)
                .orElse("Student");
    }
}