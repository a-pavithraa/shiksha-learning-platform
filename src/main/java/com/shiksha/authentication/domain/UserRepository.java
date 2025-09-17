package com.shiksha.authentication.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    List<User> findByRole(UserRole role);
    
    List<User> findByIsActive(Boolean isActive);
    
    List<User> findByRoleAndIsActive(UserRole role, Boolean isActive);
    
    boolean existsByEmail(String email);

    // Cross-module query methods for notification system
    
    /**
     * Find students enrolled in a specific subject and grade level
     * This query joins with user_subjects table to find enrolled students
     */
    @Query("""
        SELECT DISTINCT u FROM User u 
        JOIN UserSubject us ON u.id = us.userId 
        WHERE u.role = 'STUDENT' 
        AND u.gradeLevel = :gradeLevel 
        AND us.subjectId = :subjectId 
        AND u.isActive = true
        """)
    List<User> findStudentsBySubjectAndGrade(@Param("subjectId") Long subjectId, 
                                           @Param("gradeLevel") Integer gradeLevel);

    // Additional helper methods for cross-module access
    Optional<User> findByIdAndRoleAndIsActive(Long id, UserRole role, Boolean isActive);
    
    Optional<User> findByIdAndIsActive(Long id, Boolean isActive);
}