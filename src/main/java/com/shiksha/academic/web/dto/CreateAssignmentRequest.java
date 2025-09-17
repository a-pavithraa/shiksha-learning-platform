package com.shiksha.academic.web.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public class CreateAssignmentRequest {

    @NotNull(message = "Subject ID is required")
    private Long subjectId;

    @NotNull(message = "Grade level is required")
    @Min(value = 9, message = "Grade level must be between 9 and 12")
    @Max(value = 12, message = "Grade level must be between 9 and 12")
    private Integer gradeLevel;

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title cannot exceed 255 characters")
    private String title;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    private String dueDate;

    // Constructors
    public CreateAssignmentRequest() {}

    public CreateAssignmentRequest(Long subjectId, Integer gradeLevel, String title, String description, String dueDate) {
        this.subjectId = subjectId;
        this.gradeLevel = gradeLevel;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
    }

    // Getters and Setters
    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public Integer getGradeLevel() {
        return gradeLevel;
    }

    public void setGradeLevel(Integer gradeLevel) {
        this.gradeLevel = gradeLevel;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }
}