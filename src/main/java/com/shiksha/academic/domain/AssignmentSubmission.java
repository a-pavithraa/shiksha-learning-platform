package com.shiksha.academic.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "assignment_submissions")
public class AssignmentSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "assignment_id", nullable = false)
    private Long assignmentId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "submission_status_enum")
    private SubmissionStatus status;

    @Column(name = "is_late", nullable = false)
    private boolean isLate;

    @Column(name = "is_resubmission", nullable = false)
    private boolean isResubmission;

    @Column(name = "teacher_feedback", columnDefinition = "TEXT")
    private String teacherFeedback;

    // Default constructor for JPA
    protected AssignmentSubmission() {}

    // Constructor for new submissions
    public AssignmentSubmission(Long assignmentId, Long studentId, String filePath, 
                               String fileName, Long fileSize, boolean isLate, 
                               boolean isResubmission) {
        this.assignmentId = assignmentId;
        this.studentId = studentId;
        this.filePath = filePath;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.isLate = isLate;
        this.isResubmission = isResubmission;
        this.submittedAt = LocalDateTime.now();
        this.status = SubmissionStatus.SUBMITTED;
    }



    // Getters
    public Long getId() {
        return id;
    }

    public Long getAssignmentId() {
        return assignmentId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public SubmissionStatus getStatus() {
        return status;
    }

    public boolean isLate() {
        return isLate;
    }

    public boolean isResubmission() {
        return isResubmission;
    }

    public String getTeacherFeedback() {
        return teacherFeedback;
    }

    // Setters for business operations
    public void updateFile(String filePath, String fileName, Long fileSize) {
        this.filePath = filePath;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.submittedAt = LocalDateTime.now();
        this.isResubmission = true;
    }

    public void setStatus(SubmissionStatus status) {
        this.status = status;
    }

    public void setTeacherFeedback(String teacherFeedback) {
        this.teacherFeedback = teacherFeedback;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AssignmentSubmission)) return false;
        AssignmentSubmission that = (AssignmentSubmission) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "AssignmentSubmission{" +
                "id=" + id +
                ", assignmentId=" + assignmentId +
                ", studentId=" + studentId +
                ", fileName='" + fileName + '\'' +
                ", submittedAt=" + submittedAt +
                ", status=" + status +
                ", isLate=" + isLate +
                ", isResubmission=" + isResubmission +
                '}';
    }
}