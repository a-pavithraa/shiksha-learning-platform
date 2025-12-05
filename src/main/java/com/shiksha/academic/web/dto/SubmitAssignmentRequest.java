package com.shiksha.academic.web.dto;

import com.shiksha.academic.domain.command.SubmitAssignmentCommand;
import org.springframework.web.multipart.MultipartFile;

/**
 * Request DTO for assignment submission.
 * Note: This is primarily used for documentation purposes since 
 * multipart file uploads are handled directly via @RequestParam.
 */
public record SubmitAssignmentRequest(
        MultipartFile file
) {
    
    /**
     * Convert request to command object
     */
    public SubmitAssignmentCommand toCommand(Long assignmentId, Long studentId) {
        return SubmitAssignmentCommand.create(assignmentId, studentId, file);
    }

    /**
     * Validation helper
     */
    public boolean isValid() {
        return file != null && !file.isEmpty();
    }
}