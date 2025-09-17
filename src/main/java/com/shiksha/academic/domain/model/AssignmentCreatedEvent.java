package com.shiksha.academic.domain.model;

import com.shiksha.academic.domain.Assignment;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
/**
 * Domain event published when a new assignment is created
 * Following Spring Modulith event-driven architecture
 */
public  record AssignmentCreatedEvent(
                Long assignmentId,
                Long teacherId,
                Long subjectId,
                Integer gradeLevel,
                String title,
                String description,
                String fileName,
                LocalDate dueDate,
                LocalDateTime createdAt
        ) {

    // Factory method for easy creation from Assignment entity
    public static AssignmentCreatedEvent from(Assignment assignment) {
        return new AssignmentCreatedEvent(
                assignment.getId(),
                assignment.getTeacherId(),
                assignment.getSubjectId(),
                assignment.getGradeLevel(),
                assignment.getTitle(),
                assignment.getDescription(),
                assignment.getFileName(),
                assignment.getDueDate(),
                assignment.getCreatedAt()
        );
    }

    // Convenience methods for event listeners
    public String id() {
        return "assignment-" + assignmentId;
    }

    public String getTeacherSubjectGradeKey() {
        return String.format("teacher_%d_subject_%d_grade_%d", teacherId, subjectId, gradeLevel);
    }
}