package com.shiksha.academic.domain;

import com.shiksha.academic.domain.model.AssignmentCreatedEvent;
import com.shiksha.common.models.PagedResult;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final S3FileService s3FileService;
    private final ApplicationEventPublisher eventPublisher;
    private final int DEFAULT_PAGE_SIZE = 10;

    public AssignmentService(AssignmentRepository assignmentRepository, 
                           S3FileService s3FileService,
                           ApplicationEventPublisher eventPublisher) {
        this.assignmentRepository = assignmentRepository;
        this.s3FileService = s3FileService;
        this.eventPublisher = eventPublisher;
    }

    public Assignment createAssignment(Long teacherId, Long subjectId, Integer gradeLevel,
                                     String title, String description, LocalDate dueDate,
                                     MultipartFile file) throws IOException {
        
        // Validate input
        validateAssignmentInput(teacherId, subjectId, gradeLevel, title, file);
        
        // Upload file to S3
        String filePath = s3FileService.uploadFile(file, "assignments");
        
        // Create assignment entity
        Assignment assignment = new Assignment(
                teacherId, subjectId, gradeLevel, title, description,
                filePath, file.getOriginalFilename(), dueDate
        );
        
        // Save to database
        Assignment savedAssignment = assignmentRepository.save(assignment);
        
        // Publish domain event for notification module
        AssignmentCreatedEvent event = AssignmentCreatedEvent.from(savedAssignment);
        eventPublisher.publishEvent(event);
        
        System.out.println("🚀 Published AssignmentCreatedEvent for assignment: " + savedAssignment.getTitle());
        
        return savedAssignment;
    }

    @Transactional(readOnly = true)
    public List<Assignment> findActiveAssignments() {
        return assignmentRepository.findByIsActiveTrue();
    }

    @Transactional(readOnly = true)
    public List<Assignment> findAssignmentsByTeacher(Long teacherId) {
        return assignmentRepository.findByTeacherIdAndIsActiveTrue(teacherId);
    }


    @Transactional(readOnly = true)
    public PagedResult<Assignment> findAssignmentsBySubjectAndGrade(Long subjectId, Integer gradeLevel, int pageNo) {
        Pageable pageable = Pageable.ofSize(DEFAULT_PAGE_SIZE).withPage(pageNo - 1);
        var results = assignmentRepository.findBySubjectIdAndGradeLevelAndIsActiveTrue(subjectId, gradeLevel, pageable);
        return new PagedResult<>(results);

    }

    @Transactional(readOnly = true)
    public Optional<Assignment> findById(Long id) {
        return assignmentRepository.findByIdAndIsActiveTrue(id);
    }

    @Transactional(readOnly = true)
    public String generateDownloadUrl(Long assignmentId) {
        Assignment assignment = assignmentRepository.findByIdAndIsActiveTrue(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found"));
        
        return s3FileService.generatePresignedDownloadUrl(assignment.getFilePath());
    }

    public Assignment updateAssignment(Long id, String title, String description, LocalDate dueDate) {
        Assignment assignment = assignmentRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found"));
        
        if (title != null && !title.trim().isEmpty()) {
            assignment.setTitle(title.trim());
        }
        if (description != null) {
            assignment.setDescription(description.trim());
        }
        if (dueDate != null) {
            assignment.setDueDate(dueDate);
        }
        
        return assignmentRepository.save(assignment);
    }

    public void deleteAssignment(Long id) {
        Assignment assignment = assignmentRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found"));
        
        // Soft delete
        assignment.setIsActive(false);
        assignmentRepository.save(assignment);
        
        // Optionally delete file from S3
        // s3FileService.deleteFile(assignment.getFilePath());
    }

    private void validateAssignmentInput(Long teacherId, Long subjectId, Integer gradeLevel, 
                                       String title, MultipartFile file) {
        if (teacherId == null) {
            throw new IllegalArgumentException("Teacher ID cannot be null");
        }
        if (subjectId == null) {
            throw new IllegalArgumentException("Subject ID cannot be null");
        }
        if (gradeLevel == null || gradeLevel < 9 || gradeLevel > 12) {
            throw new IllegalArgumentException("Grade level must be between 9 and 12");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }
    }



}
