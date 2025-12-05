package com.shiksha.academic.domain.dto;

import com.shiksha.academic.domain.AssignmentSubmission;
import com.shiksha.academic.domain.SubmissionStatus;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for AssignmentSubmission entity.
 * Used to transfer data between service and presentation layers
 * without exposing the entity directly.
 */
public record AssignmentSubmissionDto(
        Long submissionId,
        Long assignmentId,
        Long studentId,
        String fileName,
        Long fileSize,
        LocalDateTime submissionTimestamp,
        boolean isLate,
        boolean isResubmission,
        SubmissionStatus status,
        String teacherFeedback
) {

    /**
     * Factory method to create DTO from entity
     */
    public static AssignmentSubmissionDto from(AssignmentSubmission entity) {
        return new AssignmentSubmissionDto(
                entity.getId(),
                entity.getAssignmentId(),
                entity.getStudentId(),
                entity.getFileName(),
                entity.getFileSize(),
                entity.getSubmittedAt(),
                entity.isLate(),
                entity.isResubmission(),
                entity.getStatus(),
                entity.getTeacherFeedback()
        );
    }

    /**
     * Simplified constructor for basic submission response
     */
    public AssignmentSubmissionDto(Long submissionId, Long assignmentId, Long studentId,
                                   String fileName, Long fileSize, LocalDateTime submissionTimestamp,
                                   boolean isLate, boolean isResubmission) {
        this(submissionId, assignmentId, studentId, fileName, fileSize, submissionTimestamp,
             isLate, isResubmission, SubmissionStatus.SUBMITTED, null);
    }
}