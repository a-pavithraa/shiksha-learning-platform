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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.profiles.active=test")
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@DirtiesContext
@Transactional
class AssignmentSubmissionIntegrationTest {

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
    private Assignment assignment;
    private String studentToken;

    @BeforeEach
    void setUp() {
        // Create teacher
        User teacher = new User(
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
                "Math Homework",
                "Complete chapter 5 exercises",
                "/assignments/math-homework.pdf",
                "math-homework.pdf",
                LocalDate.now().plusDays(7) // Due in 7 days
        );
        assignment = assignmentRepository.save(assignment);

        // Generate JWT token for student
        studentToken = tokenProvider.generateAccessToken(student);
    }

    @Test
    void submitAssignment_ShouldCreateSubmission_WhenValidPdfAndOnTime() throws Exception {
        // Given
        MockMultipartFile pdfFile = new MockMultipartFile(
                "file",
                "student_assignment.pdf",
                "application/pdf",
                "Student assignment content".getBytes()
        );

        // When
        mockMvc.perform(multipart("/api/assignments/{assignmentId}/submissions", assignment.getId())
                        .file(pdfFile)
                        .header("Authorization", "Bearer " + studentToken)
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(result -> {
                    System.out.println("=== TEST RESPONSE ===");
                    System.out.println("Status: " + result.getResponse().getStatus());
                    System.out.println("Body: " + result.getResponse().getContentAsString());
                    if (result.getResolvedException() != null) {
                        System.out.println("Exception: ");
                        result.getResolvedException().printStackTrace();
                    }
                })
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Assignment submitted successfully"))
                .andExpect(jsonPath("$.data.fileName").value("student_assignment.pdf"))
                .andExpect(jsonPath("$.data.isLate").value(false))
                .andExpect(jsonPath("$.data.isResubmission").value(false));

        // Then
        Optional<AssignmentSubmission> submission = assignmentSubmissionRepository
                .findByAssignmentIdAndStudentId(assignment.getId(), student.getId());
        
        assertThat(submission).isPresent();
        assertThat(submission.get().getFileName()).isEqualTo("student_assignment.pdf");
        assertThat(submission.get().isLate()).isFalse();
        assertThat(submission.get().isResubmission()).isFalse();
        assertThat(submission.get().getFileSize()).isEqualTo("Student assignment content".getBytes().length);
        assertThat(submission.get().getFilePath()).startsWith("assignment-submissions/" + assignment.getId());
    }

    @Test
    void submitAssignment_ShouldReturnBadRequest_WhenAssignmentNotFound() throws Exception {
        // Given
        Long nonExistentAssignmentId = 999L;
        MockMultipartFile pdfFile = new MockMultipartFile(
                "file",
                "assignment.pdf",
                "application/pdf",
                "Content".getBytes()
        );

        // When & Then
        mockMvc.perform(multipart("/api/assignments/{assignmentId}/submissions", nonExistentAssignmentId)
                        .file(pdfFile)
                        .header("Authorization", "Bearer " + studentToken)
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Assignment not found"))
                .andExpect(jsonPath("$.error").value("ASSIGNMENT_NOT_FOUND"));
    }

    @Test
    void submitAssignment_ShouldStoreFileInS3_WhenValidSubmission() throws Exception {
        // Given
        String fileContent = "Test PDF content for S3 storage";
        MockMultipartFile pdfFile = new MockMultipartFile(
                "file",
                "test_assignment.pdf",
                "application/pdf",
                fileContent.getBytes()
        );

        // When
        mockMvc.perform(multipart("/api/assignments/{assignmentId}/submissions", assignment.getId())
                        .file(pdfFile)
                        .header("Authorization", "Bearer " + studentToken)
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated());

        // Then - Verify database record
        Optional<AssignmentSubmission> submission = assignmentSubmissionRepository
                .findByAssignmentIdAndStudentId(assignment.getId(), student.getId());

        assertThat(submission).isPresent();
        assertThat(submission.get().getFilePath()).isNotNull();
        assertThat(submission.get().getFilePath()).contains("assignment-submissions");
        assertThat(submission.get().getFilePath()).contains(assignment.getId().toString());
        assertThat(submission.get().getFilePath()).contains(student.getId().toString());

        // Then - Verify file exists in S3
        String s3Key = submission.get().getFilePath(); // Remove leading slash
        verifyFileExistsInS3(s3Key, fileContent);
    }

    private void verifyFileExistsInS3(String key, String expectedContent) throws IOException {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket("shiksha-lms-dev-files-f8wro1wy")
                .key(key)
                .build();

        try (ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest)) {
            String actualContent = new String(s3Object.readAllBytes());
            assertThat(actualContent).isEqualTo(expectedContent);
        }
    }
}