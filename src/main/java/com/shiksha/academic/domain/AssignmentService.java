package com.shiksha.academic.domain;

import com.shiksha.academic.domain.command.CreateAssignmentCommand;
import com.shiksha.academic.domain.command.UpdateAssignmentCommand;
import com.shiksha.academic.domain.model.AssignmentCreatedEvent;
import com.shiksha.academic.domain.models.AssignmentDto;
import com.shiksha.common.models.PagedResult;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    public AssignmentDto createAssignment(CreateAssignmentCommand command) throws IOException {
        
        // Upload file to S3
        String filePath = s3FileService.uploadFile(command.file(), "assignments");
        LocalDate dueDateObj = command.dueDate() != null ? parseToLocalDate(command.dueDate()) : null;
        
        // Create assignment entity
        Assignment assignment = new Assignment(
                command.teacherId(), command.subjectId(), command.gradeLevel(), 
                command.title(), command.description(),
                filePath, command.file().getOriginalFilename(), dueDateObj
        );
        
        // Save to database
        Assignment savedAssignment = assignmentRepository.save(assignment);
        
        // Publish domain event for notification module
        AssignmentCreatedEvent event = AssignmentCreatedEvent.from(savedAssignment);
        eventPublisher.publishEvent(event);
        
        System.out.println("🚀 Published AssignmentCreatedEvent for assignment: " + savedAssignment.getTitle());
        
        return AssignmentDto.from(savedAssignment);
    }

    @Transactional(readOnly = true)
    public List<AssignmentDto> findActiveAssignments() {
        return assignmentRepository.findByIsActiveTrue().stream()
                .map(AssignmentDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public PagedResult<AssignmentDto> findActiveAssignments(int pageNo) {
        Pageable pageable = Pageable.ofSize(DEFAULT_PAGE_SIZE).withPage(pageNo - 1);
        var results = assignmentRepository.findByIsActiveTrue(pageable);
        var dtoResults = results.map(AssignmentDto::from);
        return new PagedResult<>(dtoResults);
    }

    @Transactional(readOnly = true)
    public List<AssignmentDto> findAssignmentsByTeacher(Long teacherId) {
        return assignmentRepository.findByTeacherIdAndIsActiveTrue(teacherId).stream()
                .map(AssignmentDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public PagedResult<AssignmentDto> findAssignmentsByTeacher(Long teacherId, int pageNo) {
        Pageable pageable = Pageable.ofSize(DEFAULT_PAGE_SIZE).withPage(pageNo - 1);
        var results = assignmentRepository.findByTeacherIdAndIsActiveTrue(teacherId, pageable);
        var dtoResults = results.map(AssignmentDto::from);
        return new PagedResult<>(dtoResults);
    }

    @Transactional(readOnly = true)
    public List<AssignmentDto> findAssignmentsByTeacherAndGradeLevel(Long teacherId, Integer gradeLevel) {
        return assignmentRepository.findByTeacherIdAndGradeLevelAndIsActiveTrue(teacherId, gradeLevel).stream()
                .map(AssignmentDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public PagedResult<AssignmentDto> findAssignmentsByTeacherAndGradeLevel(Long teacherId, Integer gradeLevel, int pageNo) {
        Pageable pageable = Pageable.ofSize(DEFAULT_PAGE_SIZE).withPage(pageNo - 1);
        var results = assignmentRepository.findByTeacherIdAndGradeLevelAndIsActiveTrue(teacherId, gradeLevel, pageable);
        var dtoResults = results.map(AssignmentDto::from);
        return new PagedResult<>(dtoResults);
    }


    @Transactional(readOnly = true)
    public PagedResult<AssignmentDto> findAssignmentsBySubjectAndGrade(Long subjectId, Integer gradeLevel, int pageNo) {
        Pageable pageable = Pageable.ofSize(DEFAULT_PAGE_SIZE).withPage(pageNo - 1);
        var results = assignmentRepository.findBySubjectIdAndGradeLevelAndIsActiveTrue(subjectId, gradeLevel, pageable);
        var dtoResults = results.map(AssignmentDto::from);
        return new PagedResult<>(dtoResults);
    }

    @Transactional(readOnly = true)
    public Optional<AssignmentDto> findById(Long id) {
        return assignmentRepository.findByIdAndIsActiveTrue(id)
                .map(AssignmentDto::from);
    }

    @Transactional(readOnly = true)
    public String generateDownloadUrl(Long assignmentId) {
        Assignment assignment = assignmentRepository.findByIdAndIsActiveTrue(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found"));
        
        return s3FileService.generatePresignedDownloadUrl(assignment.getFilePath());
    }

    public AssignmentDto updateAssignment(UpdateAssignmentCommand command) {
        Assignment assignment = assignmentRepository.findByIdAndIsActiveTrue(command.id())
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found"));
        
        if (command.title() != null && !command.title().trim().isEmpty()) {
            assignment.setTitle(command.title().trim());
        }
        if (command.description() != null) {
            assignment.setDescription(command.description().trim());
        }
        if (command.dueDate() != null) {
            assignment.setDueDate(command.dueDate());
        }
        
        Assignment updatedAssignment = assignmentRepository.save(assignment);
        return AssignmentDto.from(updatedAssignment);
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

    private LocalDate parseToLocalDate(String dateString) {
        if (dateString.contains("T")) {
            return LocalDateTime.parse(dateString).toLocalDate();
        } else {
            return LocalDate.parse(dateString);
        }
    }




}
