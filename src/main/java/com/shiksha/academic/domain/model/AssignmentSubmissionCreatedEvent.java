package com.shiksha.academic.domain.model;

import java.time.LocalDateTime;

/**
 * Domain event published when a student submits an assignment.
 * Used for cross-module communication (e.g., triggering notifications).
 */
public record AssignmentSubmissionCreatedEvent(
        Long submissionId,
        Long assignmentId,
        Long studentId,
        Long teacherId,
        String assignmentTitle,
        String studentName,
        String fileName,
        boolean isLate,
        boolean isResubmission,
        LocalDateTime submissionTimestamp
) {

    /**
     * Factory method to create event from submission data
     */
    public static AssignmentSubmissionCreatedEvent create(
            Long submissionId,
            Long assignmentId,
            Long studentId,
            Long teacherId,
            String assignmentTitle,
            String studentName,
            String fileName,
            boolean isLate,
            boolean isResubmission) {
        
        return new AssignmentSubmissionCreatedEvent(
                submissionId,
                assignmentId,
                studentId,
                teacherId,
                assignmentTitle,
                studentName,
                fileName,
                isLate,
                isResubmission,
                LocalDateTime.now()
        );
    }

    /**
     * Get notification message based on submission type
     */
    public String getNotificationMessage() {
        StringBuilder message = new StringBuilder();
        
        if (isResubmission) {
            message.append("RESUBMISSION: ");
        } else if (isLate) {
            message.append("LATE SUBMISSION: ");
        } else {
            message.append("NEW SUBMISSION: ");
        }
        
        message.append(studentName)
                .append(" submitted '")
                .append(fileName)
                .append("' for assignment '")
                .append(assignmentTitle)
                .append("'");
        
        return message.toString();
    }

    /**
     * Get notification priority based on submission characteristics
     */
    public NotificationPriority getPriority() {
        if (isLate) {
            return NotificationPriority.HIGH;
        } else if (isResubmission) {
            return NotificationPriority.MEDIUM;
        } else {
            return NotificationPriority.NORMAL;
        }
    }

    public enum NotificationPriority {
        NORMAL, MEDIUM, HIGH
    }
}