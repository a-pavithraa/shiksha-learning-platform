package com.shiksha.academic.domain.models;

import com.shiksha.academic.domain.Assignment;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO record for Assignment entity to be used by service layer.
 * Prevents direct exposure of entity objects to controllers.
 */
public record AssignmentDto(
    Long id,
    Long teacherId,
    Long subjectId,
    Integer gradeLevel,
    String title,
    String description,
    String filePath,
    String fileName,
    LocalDate dueDate,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    Boolean isActive
) {
    
    /**
     * Factory method to create AssignmentDto from Assignment entity.
     * Maps each property from entity to the corresponding DTO property.
     */
    public static AssignmentDto from(Assignment assignment) {
        return new AssignmentDto(
            assignment.getId(),
            assignment.getTeacherId(),
            assignment.getSubjectId(),
            assignment.getGradeLevel(),
            assignment.getTitle(),
            assignment.getDescription(),
            assignment.getFilePath(),
            assignment.getFileName(),
            assignment.getDueDate(),
            assignment.getCreatedAt(),
            assignment.getUpdatedAt(),
            assignment.getIsActive()
        );
    }
}