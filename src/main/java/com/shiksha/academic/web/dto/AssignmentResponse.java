package com.shiksha.academic.web.dto;

import com.shiksha.academic.domain.models.AssignmentDto;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response record for Assignment data to be returned to clients.
 * Maps from service layer DTOs to web layer response format.
 */
public record AssignmentResponse(
    Long id,
    Long teacherId,
    Long subjectId,
    Integer gradeLevel,
    String title,
    String description,
    String fileName,
    LocalDate dueDate,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    
    /**
     * Factory method to create AssignmentResponse from AssignmentDto.
     * Maps each property from DTO to the corresponding response property.
     */
    public static AssignmentResponse from(AssignmentDto assignmentDto) {
        return new AssignmentResponse(
            assignmentDto.id(),
            assignmentDto.teacherId(),
            assignmentDto.subjectId(),
            assignmentDto.gradeLevel(),
            assignmentDto.title(),
            assignmentDto.description(),
            assignmentDto.fileName(),
            assignmentDto.dueDate(),
            assignmentDto.createdAt(),
            assignmentDto.updatedAt()
        );
    }
}