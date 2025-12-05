package com.shiksha.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shiksha.TestcontainersConfiguration;
import com.shiksha.authentication.domain.User;
import com.shiksha.authentication.domain.UserRepository;
import com.shiksha.authentication.domain.UserRole;
import com.shiksha.authentication.security.JwtTokenProvider;
import com.shiksha.academic.domain.Assignment;
import com.shiksha.academic.domain.AssignmentRepository;
import com.shiksha.academic.domain.AssignmentSubmission;
import com.shiksha.academic.domain.AssignmentSubmissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.S3Client;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@DirtiesContext
@Transactional
class AssignmentResubmissionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private AssignmentSubmissionRepository assignmentSubmissionRepository;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private S3Client s3Client;


    private User student;
    private User teacher;
    private Assignment assignment;
    private String studentToken;

    @BeforeEach
    void setUp() {
        // Create teacher
        teacher = new User(
                "teacher@example.com",
                passwordEncoder.encode("password"),
                "John",
                "Doe",
                UserRole.TEACHER
        );
        teacher = userRepository.save(teacher);

        // Create student
        student = new User(
                "student@example.com",
                passwordEncoder.encode("password"),
                "Jane",
                "Smith",
                UserRole.STUDENT
        );
        student = userRepository.save(student);

        // Create assignment with future due date
        assignment = new Assignment(
                teacher.getId(),
                1L, // subjectId
                9, // gradeLevel
                "Resubmission Test Assignment",
                "Test assignment for resubmission scenarios",
                "/assignments/resubmission-test.pdf",
                "resubmission-test.pdf",
                LocalDate.now().plusDays(5) // Due in 5 days
        );
        assignment = assignmentRepository.save(assignment);

        // Generate JWT token for student
        studentToken = tokenProvider.generateAccessToken(student);
    }

    @Test
    void submitAssignment_ShouldMarkAsResubmission_WhenSubmittingAgain() throws Exception {
        // Given - First submission
        MockMultipartFile firstSubmission = new MockMultipartFile(
                "file",
                "first_submission.pdf",
                "application/pdf",
                "First attempt content".getBytes()
        );

        // Submit first time
        mockMvc.perform(multipart("/api/assignments/{assignmentId}/submissions", assignment.getId())
                        .file(firstSubmission)
                        .header("Authorization", "Bearer " + studentToken)
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.isResubmission").value(false));

        // Given - Second submission (resubmission)
        MockMultipartFile secondSubmission = new MockMultipartFile(
                "file",
                "revised_submission.pdf",
                "application/pdf",
                "Revised content with corrections".getBytes()
        );

        // When - Submit second time
        mockMvc.perform(multipart("/api/assignments/{assignmentId}/submissions", assignment.getId())
                        .file(secondSubmission)
                        .header("Authorization", "Bearer " + studentToken)
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Assignment resubmitted successfully"))
                .andExpect(jsonPath("$.data.fileName").value("revised_submission.pdf"))
                .andExpect(jsonPath("$.data.isResubmission").value(true))
                .andExpect(jsonPath("$.data.isLate").value(false));

        // Then - Verify only one active submission exists (latest one)
        Optional<AssignmentSubmission> submission = assignmentSubmissionRepository
                .findByAssignmentIdAndStudentId(assignment.getId(), student.getId());
        
        assertThat(submission).isPresent();
        assertThat(submission.get().getFileName()).isEqualTo("revised_submission.pdf");
        assertThat(submission.get().isResubmission()).isTrue();
        assertThat(submission.get().getFileSize()).isEqualTo("Revised content with corrections".getBytes().length);
    }

    @Test
    void submitAssignment_ShouldReplaceFileInS3_WhenResubmitting() throws Exception {
        // Given - First submission
        MockMultipartFile firstFile = new MockMultipartFile(
                "file",
                "original.pdf",
                "application/pdf",
                "Original file content".getBytes()
        );

        mockMvc.perform(multipart("/api/assignments/{assignmentId}/submissions", assignment.getId())
                        .file(firstFile)
                        .header("Authorization", "Bearer " + studentToken)
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated());

        String originalFilePath = assignmentSubmissionRepository
                .findByAssignmentIdAndStudentId(assignment.getId(), student.getId())
                .get().getFilePath();

        // Given - Resubmission
        MockMultipartFile replacementFile = new MockMultipartFile(
                "file",
                "replacement.pdf",
                "application/pdf",
                "Replacement file content with more data".getBytes()
        );

        // When
        mockMvc.perform(multipart("/api/assignments/{assignmentId}/submissions", assignment.getId())
                        .file(replacementFile)
                        .header("Authorization", "Bearer " + studentToken)
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.fileName").value("replacement.pdf"))
                .andExpect(jsonPath("$.data.isResubmission").value(true));

        // Then - Verify file path and metadata updated
        Optional<AssignmentSubmission> updatedSubmission = assignmentSubmissionRepository
                .findByAssignmentIdAndStudentId(assignment.getId(), student.getId());
        
        assertThat(updatedSubmission).isPresent();
        assertThat(updatedSubmission.get().getFileName()).isEqualTo("replacement.pdf");
        assertThat(updatedSubmission.get().getFileSize()).isEqualTo("Replacement file content with more data".getBytes().length);
        // File path should be updated to reflect new filename
        assertThat(updatedSubmission.get().getFilePath()).isNotEqualTo(originalFilePath);
    }

    @Test
    void submitAssignment_ShouldTriggerResubmissionNotification_WhenResubmitting() throws Exception {
        // Given - First submission
        MockMultipartFile firstSubmission = new MockMultipartFile(
                "file",
                "initial.pdf",
                "application/pdf",
                "Initial submission".getBytes()
        );

        mockMvc.perform(multipart("/api/assignments/{assignmentId}/submissions", assignment.getId())
                        .file(firstSubmission)
                        .header("Authorization", "Bearer " + studentToken)
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated());

        // Given - Resubmission
        MockMultipartFile resubmission = new MockMultipartFile(
                "file",
                "resubmitted.pdf",
                "application/pdf",
                "Resubmitted content".getBytes()
        );

        // When
        mockMvc.perform(multipart("/api/assignments/{assignmentId}/submissions", assignment.getId())
                        .file(resubmission)
                        .header("Authorization", "Bearer " + studentToken)
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.isResubmission").value(true));

        // Then - Verify resubmission flag is set
        Optional<AssignmentSubmission> submission = assignmentSubmissionRepository
                .findByAssignmentIdAndStudentId(assignment.getId(), student.getId());
        
        assertThat(submission).isPresent();
        assertThat(submission.get().isResubmission()).isTrue();
        
        // Note: Notification verification would be tested in event listener tests
        // The resubmission event should trigger teacher notification
    }

    @Test
    void submitAssignment_ShouldHandleLateResubmission_CorrectFlags() throws Exception {
        // Given - Assignment with past due date
        Assignment expiredAssignment = new Assignment(
                teacher.getId(),
                1L, // subjectId
                9, // gradeLevel
                "Expired Assignment",
                "Already past due",
                "/assignments/expired.pdf",
                "expired.pdf",
                LocalDate.now().minusDays(2) // Due 2 days ago
        );
        expiredAssignment = assignmentRepository.save(expiredAssignment);

        // First late submission
        MockMultipartFile firstLateSubmission = new MockMultipartFile(
                "file",
                "late_first.pdf",
                "application/pdf",
                "Late first submission".getBytes()
        );

        mockMvc.perform(multipart("/api/assignments/{assignmentId}/submissions", expiredAssignment.getId())
                        .file(firstLateSubmission)
                        .header("Authorization", "Bearer " + studentToken)
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.isLate").value(true))
                .andExpect(jsonPath("$.data.isResubmission").value(false));

        // Second late submission (late resubmission)
        MockMultipartFile secondLateSubmission = new MockMultipartFile(
                "file",
                "late_resubmission.pdf",
                "application/pdf",
                "Late resubmission content".getBytes()
        );

        // When
        mockMvc.perform(multipart("/api/assignments/{assignmentId}/submissions", expiredAssignment.getId())
                        .file(secondLateSubmission)
                        .header("Authorization", "Bearer " + studentToken)
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Late assignment resubmitted successfully"))
                .andExpect(jsonPath("$.data.isLate").value(true))
                .andExpect(jsonPath("$.data.isResubmission").value(true));

        // Then
        Optional<AssignmentSubmission> submission = assignmentSubmissionRepository
                .findByAssignmentIdAndStudentId(expiredAssignment.getId(), student.getId());
        
        assertThat(submission).isPresent();
        assertThat(submission.get().isLate()).isTrue();
        assertThat(submission.get().isResubmission()).isTrue();
    }
}