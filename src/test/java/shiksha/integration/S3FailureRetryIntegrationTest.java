package shiksha.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shiksha.TestcontainersConfiguration;
import com.shiksha.authentication.domain.User;
import com.shiksha.authentication.domain.UserRepository;
import com.shiksha.authentication.domain.UserRole;
import com.shiksha.authentication.security.JwtTokenProvider;
import com.shiksha.academic.domain.Assignment;
import com.shiksha.academic.domain.AssignmentRepository;
import com.shiksha.common.S3FileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Spy;
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
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@DirtiesContext
@Transactional
class S3FailureRetryIntegrationTest {

   /* @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private S3FileService s3FileService; // Mock S3 service to simulate failures
    @Autowired
    private S3Client s3Client;

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
        User student = new User(
                "student@example.com",
                passwordEncoder.encode("password"),
                "Jane",
                "Smith",
                UserRole.STUDENT
        );
        student = userRepository.save(student);

        // Create assignment
        assignment = new Assignment(
                teacher.getId(),
                1L, // subjectId
                9, // gradeLevel
                "S3 Retry Test Assignment",
                "Test assignment for S3 retry scenarios",
                "/assignments/s3-retry-test.pdf",
                "s3-retry-test.pdf",
                LocalDate.now().plusDays(7)
        );
        assignment = assignmentRepository.save(assignment);

        // Generate JWT token for student
        studentToken = tokenProvider.generateAccessToken(student);
    }

    @Test
    void submitAssignment_ShouldRetryOnS3Failure_AndEventuallySucceed() throws Exception {
        // Given - S3 fails twice, then succeeds on third attempt
       *//* when(s3FileService.uploadFile(anyString(), any()))
                .thenThrow(S3Exception.builder().message("Network error").build())
                .thenThrow(S3Exception.builder().message("Service unavailable").build())
                .thenReturn("https://s3.amazonaws.com/bucket/assignment-submissions/1/1/retry_test.pdf");*//*

        MockMultipartFile pdfFile = new MockMultipartFile(
                "file",
                "retry_test.pdf",
                "application/pdf",
                "Test file for retry mechanism".getBytes()
        );

        // When
        mockMvc.perform(multipart("/api/assignments/{assignmentId}/submissions", assignment.getId())
                        .file(pdfFile)
                        .header("Authorization", "Bearer " + studentToken)
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fileName").value("retry_test.pdf"));

        // Then - Verify S3 service was called 3 times (2 failures + 1 success)
        var s3ServiceSpy = spy(new S3FileService(s3Client,"shiksha-lms-dev-files-f8wro1wy","us-east-1"));
        verify(s3ServiceSpy, times(3)).uploadFile(anyString(), any());
    }

    @Test
    void submitAssignment_ShouldFailAfterMaxRetries_WhenS3PersistentFailure() throws Exception {
        // Given - S3 consistently fails beyond retry limit
        when(s3FileService.uploadFile(anyString(), any()))
                .thenThrow(S3Exception.builder().message("Persistent S3 failure").build());

        MockMultipartFile pdfFile = new MockMultipartFile(
                "file",
                "persistent_failure.pdf",
                "application/pdf",
                "Test file for persistent failure".getBytes()
        );

        // When
        mockMvc.perform(multipart("/api/assignments/{assignmentId}/submissions", assignment.getId())
                        .file(pdfFile)
                        .header("Authorization", "Bearer " + studentToken)
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Failed to store assignment file"))
                .andExpect(jsonPath("$.error").value("STORAGE_ERROR"));

        // Then - Verify S3 service was called maximum retry times (3 times)
        verify(s3FileService, times(3)).uploadFile(anyString(), any());
    }

    @Test
    void submitAssignment_ShouldHandleS3TimeoutError_WithRetry() throws Exception {
        // Given - S3 timeout error followed by success
        when(s3FileService.uploadFile(anyString(), any()))
                .thenThrow(S3Exception.builder().message("Request timeout").build())
                .thenReturn("https://s3.amazonaws.com/bucket/assignment-submissions/1/1/timeout_recovery.pdf");

        MockMultipartFile pdfFile = new MockMultipartFile(
                "file",
                "timeout_recovery.pdf",
                "application/pdf",
                "Test file for timeout recovery".getBytes()
        );

        // When
        mockMvc.perform(multipart("/api/assignments/{assignmentId}/submissions", assignment.getId())
                        .file(pdfFile)
                        .header("Authorization", "Bearer " + studentToken)
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fileName").value("timeout_recovery.pdf"));

        // Then - Verify retry mechanism worked
        verify(s3FileService, times(2)).uploadFile(anyString(), any());
    }

    @Test
    void submitAssignment_ShouldHandleAuthenticationError_NoRetry() throws Exception {
        // Given - S3 authentication error (should not be retried)
        when(s3FileService.uploadFile(anyString(), any()))
                .thenThrow(S3Exception.builder()
                        .statusCode(403)
                        .message("Access denied")
                        .build());

        MockMultipartFile pdfFile = new MockMultipartFile(
                "file",
                "auth_error.pdf",
                "application/pdf",
                "Test file for auth error".getBytes()
        );

        // When
        mockMvc.perform(multipart("/api/assignments/{assignmentId}/submissions", assignment.getId())
                        .file(pdfFile)
                        .header("Authorization", "Bearer " + studentToken)
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("STORAGE_ERROR"));

        // Then - Verify no retries for authentication errors
        verify(s3FileService, times(1)).uploadFile(anyString(), any());
    }

    @Test
    void submitAssignment_ShouldSucceedOnFirstTry_WhenS3Works() throws Exception {
        // Given - S3 works perfectly on first attempt
        when(s3FileService.uploadFile(anyString(), any()))
                .thenReturn("https://s3.amazonaws.com/bucket/assignment-submissions/1/1/first_try_success.pdf");

        MockMultipartFile pdfFile = new MockMultipartFile(
                "file",
                "first_try_success.pdf",
                "application/pdf",
                "Test file for immediate success".getBytes()
        );

        // When
        mockMvc.perform(multipart("/api/assignments/{assignmentId}/submissions", assignment.getId())
                        .file(pdfFile)
                        .header("Authorization", "Bearer " + studentToken)
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fileName").value("first_try_success.pdf"));

        // Then - Verify only one call to S3 service
        verify(s3FileService, times(1)).uploadFile(anyString(), any());
    }

    @Test
    void submitAssignment_ShouldLogRetryAttempts_ForMonitoring() throws Exception {
        // Given - S3 fails once, then succeeds
        when(s3FileService.uploadFile(anyString(), any()))
                .thenThrow(S3Exception.builder().message("Transient error").build())
                .thenReturn("https://s3.amazonaws.com/bucket/assignment-submissions/1/1/retry_logged.pdf");

        MockMultipartFile pdfFile = new MockMultipartFile(
                "file",
                "retry_logged.pdf",
                "application/pdf",
                "Test file for retry logging".getBytes()
        );

        // When
        mockMvc.perform(multipart("/api/assignments/{assignmentId}/submissions", assignment.getId())
                        .file(pdfFile)
                        .header("Authorization", "Bearer " + studentToken)
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));

        // Then - Verify retry mechanism was triggered
        verify(s3FileService, times(2)).uploadFile(anyString(), any());
        
        // Note: In a real implementation, this test would also verify that
        // retry attempts are properly logged for monitoring and debugging
    }*/
}