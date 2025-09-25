package com.shiksha.academic.domain.command;

import java.time.LocalDate;

/**
 * Command object for updating an existing assignment.
 * Encapsulates all parameters needed for assignment update.
 */
public record UpdateAssignmentCommand(
        Long id,
        String title,
        String description,
        LocalDate dueDate
) {
}