package com.shiksha.academic.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    // Find active assignments
    List<Assignment> findByIsActiveTrue();

    // Find assignments by teacher
    List<Assignment> findByTeacherIdAndIsActiveTrue(Long teacherId);

    // Find assignments by subject and grade level
    List<Assignment> findBySubjectIdAndGradeLevelAndIsActiveTrue(Long subjectId, Integer gradeLevel);

    // Find assignments by subject and grade level with pagination
    Page<Assignment> findBySubjectIdAndGradeLevelAndIsActiveTrue(Long subjectId, Integer gradeLevel, Pageable pageable);

    // Find assignments for a specific teacher, subject, and grade level
    @Query("SELECT a FROM Assignment a WHERE a.teacherId = :teacherId AND a.subjectId = :subjectId AND a.gradeLevel = :gradeLevel AND a.isActive = true ORDER BY a.createdAt DESC")
    List<Assignment> findByTeacherAndSubjectAndGradeLevel(@Param("teacherId") Long teacherId, 
                                                           @Param("subjectId") Long subjectId, 
                                                           @Param("gradeLevel") Integer gradeLevel);

    // Find assignments due on or before a specific date
    @Query("SELECT a FROM Assignment a WHERE a.dueDate <= :dueDate AND a.isActive = true ORDER BY a.dueDate ASC")
    List<Assignment> findAssignmentsDueBefore(@Param("dueDate") LocalDate dueDate);

    // Find assignments by grade level
    List<Assignment> findByGradeLevelAndIsActiveTrue(Integer gradeLevel);

    // Find assignment by id and active status
    Optional<Assignment> findByIdAndIsActiveTrue(Long id);

    // Count assignments by teacher
    @Query("SELECT COUNT(a) FROM Assignment a WHERE a.teacherId = :teacherId AND a.isActive = true")
    Long countByTeacherId(@Param("teacherId") Long teacherId);

    // Find recent assignments by teacher
    @Query("SELECT a FROM Assignment a WHERE a.teacherId = :teacherId AND a.isActive = true ORDER BY a.createdAt DESC")
    List<Assignment> findRecentByTeacherId(@Param("teacherId") Long teacherId, Pageable pageable);
}