package com.shiksha.academic.web;

import com.shiksha.academic.domain.Assignment;
import com.shiksha.academic.domain.AssignmentService;
import com.shiksha.academic.domain.command.CreateAssignmentCommand;
import com.shiksha.academic.domain.models.AssignmentDto;
import com.shiksha.academic.web.dto.AssignmentResponse;
import com.shiksha.academic.web.dto.CreateAssignmentRequest;
import com.shiksha.authentication.domain.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/assignments")
public class AssignmentController {

    private static final Logger logger = LoggerFactory.getLogger(AssignmentController.class);
    private final AssignmentService assignmentService;

    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createAssignment(
            @Valid @ModelAttribute CreateAssignmentRequest request,
            @RequestParam("file") MultipartFile file) {

        try {
            // Validate file at controller level
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "File cannot be empty"
                ));
            }
            
            System.out.println("Inside createAssignment() with request: " + request + " and file: " + file.getOriginalFilename() + " of size: " + file.getSize() + " bytes");
            // Get current user ID from security context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long teacherId = extractUserIdFromAuthentication(authentication);
            
            // Create assignment command
            CreateAssignmentCommand command = new CreateAssignmentCommand(
                    teacherId,
                    request.getSubjectId(),
                    request.getGradeLevel(),
                    request.getTitle(),
                    request.getDescription(),
                    request.getDueDate(),
                    file
            );
            
            // Create assignment
            AssignmentDto assignment = assignmentService.createAssignment(command);
            
            // Return response
            AssignmentResponse response = AssignmentResponse.from(assignment);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "status", "success",
                    "message", "Assignment created successfully",
                    "data", response
            ));
            
        } catch (IOException e) {
            logger.error("File upload failed for assignment creation", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "File upload failed: " + e.getMessage()
            ));
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid argument for assignment creation: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            logger.error("Unexpected error during assignment creation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", "error",
                    "message", "An unexpected error occurred"
            ));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAssignment(@PathVariable Long id) {
        try {
            AssignmentDto assignment = assignmentService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Assignment not found"));
            
            AssignmentResponse response = AssignmentResponse.from(assignment);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "data", response
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<?> downloadAssignment(@PathVariable Long id) {
        try {
            String downloadUrl = assignmentService.generateDownloadUrl(id);
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", downloadUrl)
                    .build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/student")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> getAssignmentsForStudent(
            @RequestParam Integer gradeLevel,
            @RequestParam List<Long> subjectIds,
            @RequestParam(defaultValue = "1") int page) {
        
        try {
            // Get current user ID from authentication
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long studentId = extractUserIdFromAuthentication(authentication);

            // TODO: Add validation that the student is actually enrolled in the requested subjects

            
            // Get assignments for all requested subjects at the student's grade level
            List<AssignmentResponse> allAssignments = new ArrayList<>();
            int totalPages = 0;
            long totalElements = 0;
            boolean hasNext = false;
            boolean hasPrevious = false;
            
            for (Long subjectId : subjectIds) {
                var pagedResult = assignmentService.findAssignmentsBySubjectAndGrade(subjectId, gradeLevel, page);
                List<AssignmentResponse> assignments = pagedResult.data().stream()
                        .map(AssignmentResponse::from)
                        .toList();
                allAssignments.addAll(assignments);
                
                // Aggregate pagination metadata
                totalPages = Math.max(totalPages, pagedResult.totalPages());
                totalElements += pagedResult.totalElements();
                hasNext = hasNext || pagedResult.hasNext();
                hasPrevious = hasPrevious || pagedResult.hasPrevious();
            }
            
            // Sort by creation date (newest first)
            allAssignments.sort((a, b) -> b.createdAt().compareTo(a.createdAt()));
            
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "data", Map.of(
                            "assignments", allAssignments,
                            "gradeLevel", gradeLevel,
                            "requestedSubjects", subjectIds,
                            "totalPages", totalPages,
                            "currentPage", page,
                            "totalElements", totalElements,
                            "hasNext", hasNext,
                            "hasPrevious", hasPrevious
                    )
            ));
            
        } catch (Exception e) {
            logger.error("Failed to retrieve assignments for student", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", "error",
                    "message", "Failed to retrieve assignments"
            ));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllActiveAssignments(@RequestParam(defaultValue = "1") int page) {
        try {
            var pagedResult = assignmentService.findActiveAssignments(page);
            List<AssignmentResponse> assignments = pagedResult.data().stream()
                    .map(AssignmentResponse::from)
                    .toList();
            
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "data", Map.of(
                            "assignments", assignments,
                            "totalPages", pagedResult.totalPages(),
                            "currentPage", page,
                            "totalElements", pagedResult.totalElements(),
                            "hasNext", pagedResult.hasNext(),
                            "hasPrevious", pagedResult.hasPrevious()
                    )
            ));
        } catch (Exception e) {
            logger.error("Failed to retrieve active assignments", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", "error",
                    "message", "Failed to retrieve assignments"
            ));
        }
    }

    @GetMapping("/teacher")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> getAssignmentsByTeacher(
            @RequestParam(required = false) Integer gradeLevel,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page) {
        
        try {
            // Get current user ID from authentication
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long teacherId = extractUserIdFromAuthentication(authentication);

            
            var pagedResult = (gradeLevel != null) 
                ? assignmentService.findAssignmentsByTeacherAndGradeLevel(teacherId, gradeLevel, page)
                : assignmentService.findAssignmentsByTeacher(teacherId, page);
            
            List<AssignmentResponse> assignments = pagedResult.data().stream()
                    .map(AssignmentResponse::from)
                    .toList();
            
           return ResponseEntity.ok(Map.of(
                   "status", "success",
                   "data", Map.of(
                           "assignments", assignments,
                           "totalPages", pagedResult.totalPages(),
                           "currentPage", page,
                           "totalElements", pagedResult.totalElements(),
                           "hasNext", pagedResult.hasNext(),
                           "hasPrevious", pagedResult.hasPrevious()
                   )
           ));
            
        } catch (Exception e) {
            logger.error("Failed to retrieve teacher assignments", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", "error",
                    "message", "Failed to retrieve teacher assignments"
            ));
        }
    }

    private Long extractUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User must be authenticated");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof User user) {
            if (user.getId() == null) {
                throw new IllegalStateException("Authenticated user does not have an ID");
            }
            return user.getId();
        }
        if (principal instanceof UserDetails) {
            // Principal is a UserDetails without ID information (unexpected for our app)
            throw new IllegalStateException("Unable to extract user ID from authentication principal");
        }
        throw new IllegalStateException("Unsupported authentication principal type: " + principal.getClass().getName());
    }
}
