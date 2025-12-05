package com.shiksha.academic.web.controller;

import com.shiksha.academic.domain.AssignmentSubmissionService;
import com.shiksha.academic.domain.command.SubmitAssignmentCommand;
import com.shiksha.academic.domain.dto.AssignmentSubmissionDto;
import com.shiksha.authentication.UserServiceAPI;
import com.shiksha.authentication.domain.User;
import com.shiksha.authentication.domain.models.UserDto;
import com.shiksha.common.models.ApiResponse;
import com.shiksha.config.FileValidation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/assignments")
public class AssignmentSubmissionController {

    private final AssignmentSubmissionService submissionService;
    private final UserServiceAPI userServiceAPI;

    public AssignmentSubmissionController(AssignmentSubmissionService submissionService, UserServiceAPI userServiceAPI) {
        this.submissionService = submissionService;
        this.userServiceAPI = userServiceAPI;
    }

    /**
     * Submit assignment by student
     */
    @PostMapping("/{assignmentId}/submissions")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<AssignmentSubmissionDto>> submitAssignment(
            @PathVariable Long assignmentId,
            @RequestParam("file") @FileValidation.ValidAssignmentFile MultipartFile file) {

        try {
            // Get current user ID from security context
            Long studentId = getCurrentUserId();

            // Create command
            SubmitAssignmentCommand command = SubmitAssignmentCommand.create(assignmentId, studentId, file);

            // Submit assignment
            AssignmentSubmissionDto submissionDto = submissionService.submitAssignment(command);

            // Determine success message based on submission type
            String message = getSuccessMessage(submissionDto.isLate(), submissionDto.isResubmission());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(message, submissionDto));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage(), getErrorCode(e.getMessage())));
        } catch (Exception e) {
            e.printStackTrace(); // Temporary debugging
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to store assignment file", "STORAGE_ERROR"));
        }
    }

    /**
     * Get submission for an assignment by current student
     */
    @GetMapping("/{assignmentId}/submissions/my")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<AssignmentSubmissionDto>> getMySubmission(
            @PathVariable Long assignmentId) {

        Long studentId = getCurrentUserId();

        return submissionService.getSubmission(assignmentId, studentId)
                .map(dto -> ResponseEntity.ok(
                        ApiResponse.success("Submission retrieved successfully", dto)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("No submission found", "SUBMISSION_NOT_FOUND")));
    }

    /**
     * Get all submissions for an assignment (teachers only)
     */
    @GetMapping("/{assignmentId}/submissions")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<List<AssignmentSubmissionDto>>> getAssignmentSubmissions(
            @PathVariable Long assignmentId) {

        List<AssignmentSubmissionDto> submissions = submissionService.getSubmissionsForAssignment(assignmentId);

        return ResponseEntity.ok(
                ApiResponse.success("Submissions retrieved successfully", submissions));
    }

    /**
     * Get all submissions by current student
     */
    @GetMapping("/submissions/my")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<List<AssignmentSubmissionDto>>> getMySubmissions() {

        Long studentId = getCurrentUserId();

        List<AssignmentSubmissionDto> submissions = submissionService.getSubmissionsByStudent(studentId);

        return ResponseEntity.ok(
                ApiResponse.success("Submissions retrieved successfully", submissions));
    }

    /**
     * Get submissions requiring grading (teachers only)
     */
    @GetMapping("/submissions/requiring-grading")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<List<AssignmentSubmissionDto>>> getSubmissionsRequiringGrading() {

        Long teacherId = getCurrentUserId();

        List<AssignmentSubmissionDto> submissions = submissionService.getSubmissionsRequiringGrading(teacherId);

        return ResponseEntity.ok(
                ApiResponse.success("Submissions requiring grading retrieved successfully", submissions));
    }

    // Private helper methods

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("User not authenticated");
        }

        Object principal = authentication.getPrincipal();

        // Principal is User (which implements UserDetails)
        if (principal instanceof UserDetails userDetails) {
            // Use UserServiceAPI to get user ID by username (email)
            String username = userDetails.getUsername();
            UserDto userDto = userServiceAPI.findByUsername(username);
            if (userDto.id() == null) {
                throw new IllegalStateException("Authenticated user does not have an ID");
            }
            return userDto.id();
        }

        throw new SecurityException("Unable to determine user ID");
    }

    private String getSuccessMessage(boolean isLate, boolean isResubmission) {
        if (isResubmission && isLate) {
            return "Late assignment resubmitted successfully";
        } else if (isResubmission) {
            return "Assignment resubmitted successfully";
        } else if (isLate) {
            return "Late assignment submitted successfully";
        } else {
            return "Assignment submitted successfully";
        }
    }

    private String getErrorCode(String errorMessage) {
        if (errorMessage.contains("PDF format")) {
            return "INVALID_FILE_TYPE";
        } else if (errorMessage.contains("5MB limit")) {
            return "FILE_TOO_LARGE";
        } else if (errorMessage.contains("empty")) {
            return "EMPTY_FILE";
        } else if (errorMessage.contains("Assignment not found")) {
            return "ASSIGNMENT_NOT_FOUND";
        } else if (errorMessage.contains("Student not found")) {
            return "STUDENT_NOT_FOUND";
        } else if (errorMessage.contains("Only students can submit")) {
            return "FORBIDDEN";
        } else if (errorMessage.contains("not enrolled")) {
            return "STUDENT_NOT_ENROLLED";
        } else if (errorMessage.contains("extension")) {
            return "INVALID_FILE_EXTENSION";
        } else if (errorMessage.contains("required")) {
            return "MISSING_FILE";
        } else {
            return "VALIDATION_ERROR";
        }
    }
}