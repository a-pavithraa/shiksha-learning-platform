package com.shiksha.academic.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.shiksha.academic.domain.SubmissionStatus;
import com.shiksha.academic.domain.dto.AssignmentSubmissionDto;

import java.time.LocalDateTime;

/**
 * Response DTO for assignment submission operations.
 * Used to return submission data to the client.
 */
public record AssignmentSubmissionResponse(
        Long submissionId,
        Long assignmentId,
        String fileName,
        Long fileSize,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime submissionTimestamp,
        boolean isLate,
        boolean isResubmission,
        SubmissionStatus status,
        String teacherFeedback
) {

    /**
     * Factory method to create response from DTO
     */
    public static AssignmentSubmissionResponse from(AssignmentSubmissionDto dto) {
        return new AssignmentSubmissionResponse(
                dto.submissionId(),
                dto.assignmentId(),
                dto.fileName(),
                dto.fileSize(),
                dto.submissionTimestamp(),
                dto.isLate(),
                dto.isResubmission(),
                dto.status(),
                dto.teacherFeedback()
        );
    }

    /**
     * Simplified constructor for basic submission response (without feedback)
     */
    public AssignmentSubmissionResponse(Long submissionId, Long assignmentId, String fileName,
                                       Long fileSize, LocalDateTime submissionTimestamp,
                                       boolean isLate, boolean isResubmission, SubmissionStatus status) {
        this(submissionId, assignmentId, fileName, fileSize, submissionTimestamp,
             isLate, isResubmission, status, null);
    }
}