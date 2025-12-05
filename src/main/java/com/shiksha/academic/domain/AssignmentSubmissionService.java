package com.shiksha.academic.domain;

import com.shiksha.academic.domain.command.SubmitAssignmentCommand;
import com.shiksha.academic.domain.dto.AssignmentSubmissionDto;
import com.shiksha.academic.domain.model.AssignmentSubmissionCreatedEvent;
import com.shiksha.authentication.UserServiceAPI;
import com.shiksha.authentication.domain.models.UserDto;
import com.shiksha.common.S3FileService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AssignmentSubmissionService {

    private final AssignmentSubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;
    private final UserServiceAPI userServiceAPI;
    private final S3FileService s3FileService;
    private final ApplicationEventPublisher eventPublisher;

    public AssignmentSubmissionService(AssignmentSubmissionRepository submissionRepository,
                                     AssignmentRepository assignmentRepository,
                                     UserServiceAPI userServiceAPI,
                                     S3FileService s3FileService,
                                     ApplicationEventPublisher eventPublisher) {
        this.submissionRepository = submissionRepository;
        this.assignmentRepository = assignmentRepository;
        this.userServiceAPI = userServiceAPI;
        this.s3FileService = s3FileService;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Submit an assignment file for a student
     */
    public AssignmentSubmissionDto submitAssignment(SubmitAssignmentCommand command) {
        // Validate command
        validateSubmissionCommand(command);

        // Validate assignment exists and is active
        Assignment assignment = assignmentRepository.findById(command.assignmentId())
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found"));

        if (!assignment.isActive()) {
            throw new IllegalArgumentException("Assignment is no longer active");
        }

        // Validate student exists and has correct role
        UserDto student = userServiceAPI.findById(command.studentId()).orElseThrow(() -> new IllegalArgumentException("Student not found"));


        // Check if it's a resubmission
        Optional<AssignmentSubmission> existingSubmission = 
                submissionRepository.findByAssignmentIdAndStudentId(command.assignmentId(), command.studentId());
        boolean isResubmission = existingSubmission.isPresent();

        // Determine if submission is late
        boolean isLate = isSubmissionLate(assignment.getDueDate());

        // Generate S3 file path
        String s3Key = generateS3Key(command.assignmentId(), command.studentId(), command.fileName());

        try {
            // Upload file to S3
            String fileUrl = s3FileService.uploadFile(s3Key, command.file());

            // Create or update submission
            AssignmentSubmission submission;
            if (isResubmission) {
                submission = existingSubmission.get();
                submission.updateFile(s3Key, command.fileName(), command.fileSize());
            } else {
                submission = new AssignmentSubmission(
                        command.assignmentId(),
                        command.studentId(),
                        s3Key,
                        command.fileName(),
                        command.fileSize(),
                        isLate,
                        false
                );
            }


            // Save submission
            submission = submissionRepository.save(submission);

            // Get teacher for event
            UserDto teacher = userServiceAPI.findById(assignment.getTeacherId()).orElseThrow(() -> new IllegalArgumentException("Teacher not found"));


            // Publish domain event
            AssignmentSubmissionCreatedEvent event = AssignmentSubmissionCreatedEvent.create(
                    submission.getId(),
                    assignment.getId(),
                    student.id(),
                    teacher != null ? teacher.id() : null,
                    assignment.getTitle(),
                    student.firstName() + " " + student.lastName(),
                    command.fileName(),
                    isLate,
                    isResubmission
            );
            eventPublisher.publishEvent(event);

            return AssignmentSubmissionDto.from(submission);

        } catch (Exception e) {
            throw new RuntimeException("Failed to store assignment file", e);
        }
    }

    /**
     * Get submission by assignment and student
     */
    @Transactional(readOnly = true)
    public Optional<AssignmentSubmissionDto> getSubmission(Long assignmentId, Long studentId) {
        return submissionRepository.findByAssignmentIdAndStudentId(assignmentId, studentId)
                .map(AssignmentSubmissionDto::from);
    }

    /**
     * Get all submissions for an assignment
     */
    @Transactional(readOnly = true)
    public List<AssignmentSubmissionDto> getSubmissionsForAssignment(Long assignmentId) {
        return submissionRepository.findByAssignmentId(assignmentId)
                .stream()
                .map(AssignmentSubmissionDto::from)
                .toList();
    }

    /**
     * Get all submissions by a student
     */
    @Transactional(readOnly = true)
    public List<AssignmentSubmissionDto> getSubmissionsByStudent(Long studentId) {
        return submissionRepository.findByStudentId(studentId)
                .stream()
                .map(AssignmentSubmissionDto::from)
                .toList();
    }

    /**
     * Get submissions requiring grading for a teacher
     */
    @Transactional(readOnly = true)
    public List<AssignmentSubmissionDto> getSubmissionsRequiringGrading(Long teacherId) {
        return submissionRepository.findSubmissionsRequiringGrading(teacherId)
                .stream()
                .map(AssignmentSubmissionDto::from)
                .toList();
    }

    /**
     * Check if student has submitted for an assignment
     */
    @Transactional(readOnly = true)
    public boolean hasStudentSubmitted(Long assignmentId, Long studentId) {
        return submissionRepository.existsByAssignmentIdAndStudentId(assignmentId, studentId);
    }

    // Private helper methods

    private void validateSubmissionCommand(SubmitAssignmentCommand command) {
        if (!command.isValid()) {
            throw new IllegalArgumentException("Invalid submission command");
        }
        
        if (!command.isPdfFile()) {
            throw new IllegalArgumentException("File must be PDF format");
        }
        
        if (!command.isWithinSizeLimit()) {
            throw new IllegalArgumentException("File size exceeds 5MB limit");
        }
        
        if (command.file().isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }
    }

    private boolean isSubmissionLate(LocalDate dueDate) {
        if (dueDate == null) {
            return false; // No due date means never late
        }
        return LocalDate.now().isAfter(dueDate);
    }

    private String generateS3Key(Long assignmentId, Long studentId, String fileName) {
        return String.format("assignment-submissions/%d/%d/%s", assignmentId, studentId, fileName);
    }
}