package com.shiksha.academic.web;

import com.shiksha.academic.domain.Assignment;
import com.shiksha.academic.domain.AssignmentService;
import com.shiksha.academic.web.dto.AssignmentResponse;
import com.shiksha.academic.web.dto.CreateAssignmentRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/assignments")
public class AssignmentController {

    private final AssignmentService assignmentService;

    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createAssignment(
            @Valid @ModelAttribute CreateAssignmentRequest request,
            @RequestParam("file") MultipartFile file) {

        try {
            System.out.println("Inside createAssignment() with request: " + request + " and file: " + file.getOriginalFilename() + " of size: " + file.getSize() + " bytes");
            // Get current user ID from security context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long teacherId = extractUserIdFromAuthentication(authentication);
            
            // Create assignment
            Assignment assignment = assignmentService.createAssignment(
                    teacherId,
                    request.getSubjectId(),
                    request.getGradeLevel(),
                    request.getTitle(),
                    request.getDescription(),
                    request.getDueDate(),
                    file
            );
            
            // Return response
            AssignmentResponse response = new AssignmentResponse(assignment);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "status", "success",
                    "message", "Assignment created successfully",
                    "data", response
            ));
            
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "File upload failed: " + e.getMessage()
            ));
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", "error",
                    "message", "An unexpected error occurred"
            ));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAssignment(@PathVariable Long id) {
        try {
            Assignment assignment = assignmentService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Assignment not found"));
            
            AssignmentResponse response = new AssignmentResponse(assignment);
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

    private Long extractUserIdFromAuthentication(Authentication authentication) {
        // This is a simplified implementation
        // In a real Spring Security setup, you would extract the user ID from the authentication object
        // For now, returning a mock teacher ID for testing
        return 1L; // TODO: Extract actual user ID from JWT token or UserDetails
    }
}
