package com.shiksha.academic.domain.command;

import org.springframework.web.multipart.MultipartFile;

/**
 * Command object for creating a new assignment.
 * Encapsulates all parameters needed for assignment creation.
 */
public record CreateAssignmentCommand(
        Long teacherId,
        Long subjectId,
        Integer gradeLevel,
        String title,
        String description,
        String dueDate,
        MultipartFile file
) {
}