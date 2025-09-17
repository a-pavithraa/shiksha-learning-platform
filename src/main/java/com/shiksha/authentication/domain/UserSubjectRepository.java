package com.shiksha.authentication.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSubjectRepository extends JpaRepository<UserSubject, Long> {
    
    List<UserSubject> findByUserId(Long userId);
    
    List<UserSubject> findBySubjectId(Long subjectId);
    
    List<UserSubject> findByGradeLevel(Integer gradeLevel);
    
    Optional<UserSubject> findByUserIdAndSubjectIdAndGradeLevel(Long userId, Long subjectId, Integer gradeLevel);
    
    List<UserSubject> findByUserIdAndGradeLevel(Long userId, Integer gradeLevel);
    
    @Query("SELECT us FROM UserSubject us WHERE us.subjectId = :subjectId AND us.gradeLevel = :gradeLevel")
    List<UserSubject> findBySubjectIdAndGradeLevel(@Param("subjectId") Long subjectId, @Param("gradeLevel") Integer gradeLevel);
    
    boolean existsByUserIdAndSubjectIdAndGradeLevel(Long userId, Long subjectId, Integer gradeLevel);
}