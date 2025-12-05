package com.shiksha.academic.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssignmentSubmissionRepository extends JpaRepository<AssignmentSubmission, Long> {

    /**
     * Find submission by assignment and student ID
     */
    Optional<AssignmentSubmission> findByAssignmentIdAndStudentId(Long assignmentId, Long studentId);

    /**
     * Check if student has already submitted for an assignment
     */
    boolean existsByAssignmentIdAndStudentId(Long assignmentId, Long studentId);

    /**
     * Find all submissions for a specific assignment
     */
    List<AssignmentSubmission> findByAssignmentId(Long assignmentId);

    /**
     * Find all submissions by a specific student
     */
    List<AssignmentSubmission> findByStudentId(Long studentId);

    /**
     * Find submissions for a specific assignment with specific status
     */
    List<AssignmentSubmission> findByAssignmentIdAndStatus(Long assignmentId, SubmissionStatus status);

    /**
     * Find late submissions for an assignment
     */
    List<AssignmentSubmission> findByAssignmentIdAndIsLateTrue(Long assignmentId);

    /**
     * Find resubmissions for an assignment
     */
    List<AssignmentSubmission> findByAssignmentIdAndIsResubmissionTrue(Long assignmentId);

    /**
     * Count submissions for an assignment
     */
    long countByAssignmentId(Long assignmentId);

    /**
     * Count late submissions for an assignment
     */
    long countByAssignmentIdAndIsLateTrue(Long assignmentId);

    /**
     * Find submissions by teacher (through assignment relationship)
     */
    @Query("SELECT s FROM AssignmentSubmission s " +
           "JOIN Assignment a ON s.assignmentId = a.id " +
           "WHERE a.teacherId = :teacherId")
    List<AssignmentSubmission> findSubmissionsByTeacherId(@Param("teacherId") Long teacherId);

    /**
     * Find submissions requiring grading (submitted status)
     */
    @Query("SELECT s FROM AssignmentSubmission s " +
           "JOIN Assignment a ON s.assignmentId = a.id " +
           "WHERE a.teacherId = :teacherId AND s.status = 'SUBMITTED'")
    List<AssignmentSubmission> findSubmissionsRequiringGrading(@Param("teacherId") Long teacherId);
}