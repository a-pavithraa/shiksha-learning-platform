package com.shiksha.academic.web.dto;

import com.shiksha.academic.domain.Assignment;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class AssignmentResponse {

    private Long id;
    private Long teacherId;
    private Long subjectId;
    private Integer gradeLevel;
    private String title;
    private String description;
    private String fileName;
    private LocalDate dueDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public AssignmentResponse() {}

    public AssignmentResponse(Assignment assignment) {
        this.id = assignment.getId();
        this.teacherId = assignment.getTeacherId();
        this.subjectId = assignment.getSubjectId();
        this.gradeLevel = assignment.getGradeLevel();
        this.title = assignment.getTitle();
        this.description = assignment.getDescription();
        this.fileName = assignment.getFileName();
        this.dueDate = assignment.getDueDate();
        this.createdAt = assignment.getCreatedAt();
        this.updatedAt = assignment.getUpdatedAt();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

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

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}