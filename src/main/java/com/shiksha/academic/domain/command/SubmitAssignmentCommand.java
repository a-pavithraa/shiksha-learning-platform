package com.shiksha.academic.domain.command;

import org.springframework.web.multipart.MultipartFile;

/**
 * Command object for assignment submission containing all necessary data
 * for the service layer to process the request.
 */
public record SubmitAssignmentCommand(
        Long assignmentId,
        Long studentId,
        MultipartFile file,
        String fileName,
        Long fileSize,
        String contentType
) {
    
    /**
     * Factory method to create command from request data
     */
    public static SubmitAssignmentCommand create(Long assignmentId, Long studentId, MultipartFile file) {
        return new SubmitAssignmentCommand(
                assignmentId,
                studentId,
                file,
                file.getOriginalFilename(),
                file.getSize(),
                file.getContentType()
        );
    }

    /**
     * Validation helper method
     */
    public boolean isValid() {
        return assignmentId != null 
                && studentId != null 
                && file != null 
                && !file.isEmpty()
                && fileName != null 
                && !fileName.trim().isEmpty()
                && fileSize != null 
                && fileSize > 0
                && contentType != null;
    }

    /**
     * Check if file is PDF
     */
    public boolean isPdfFile() {
        return "application/pdf".equals(contentType) && fileName.toLowerCase().endsWith(".pdf");
    }

    /**
     * Check if file size is within limit (5MB = 5,242,880 bytes)
     */
    public boolean isWithinSizeLimit() {
        return fileSize <= 5_242_880L;
    }
}