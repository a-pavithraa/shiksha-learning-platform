package shiksha.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shiksha.TestcontainersConfiguration;
import com.shiksha.authentication.domain.User;
import com.shiksha.authentication.domain.UserRepository;
import com.shiksha.authentication.domain.UserRole;
import com.shiksha.authentication.security.JwtTokenProvider;
import com.shiksha.academic.domain.Assignment;
import com.shiksha.academic.domain.AssignmentRepository;
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

import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@DirtiesContext
@Transactional
class FileValidationIntegrationTest {

    @Autowired
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

        // Create assignment
        assignment = new Assignment(
                teacher.getId(),
                1L, // subjectId
                9, // gradeLevel
                "Validation Test Assignment",
                "Test assignment for file validation",
                "/assignments/validation-test.pdf",
                "validation-test.pdf",
                LocalDate.now().plusDays(7)
        );
        assignment = assignmentRepository.save(assignment);

        // Generate JWT token for student
        studentToken = tokenProvider.generateAccessToken(student);
    }

    @Test
    void submitAssignment_ShouldRejectNonPdfFile_WithDocxFormat() throws Exception {
        // Given
        MockMultipartFile docxFile = new MockMultipartFile(
                "file",
                "assignment.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "DOCX file content".getBytes()
        );

        // When & Then
        mockMvc.perform(multipart("/api/assignments/{assignmentId}/submissions", assignment.getId())
                        .file(docxFile)
                        .header("Authorization", "Bearer " + studentToken)
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("File must be PDF format"))
                .andExpect(jsonPath("$.error").value("INVALID_FILE_TYPE"));
    }

    @Test
    void submitAssignment_ShouldRejectNonPdfFile_WithTextFormat() throws Exception {
        // Given
        MockMultipartFile textFile = new MockMultipartFile(
                "file",
                "assignment.txt",
                "text/plain",
                "Plain text file content".getBytes()
        );

        // When & Then
        mockMvc.perform(multipart("/api/assignments/{assignmentId}/submissions", assignment.getId())
                        .file(textFile)
                        .header("Authorization", "Bearer " + studentToken)
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("File must be PDF format"))
                .andExpect(jsonPath("$.error").value("INVALID_FILE_TYPE"));
    }

    @Test
    void submitAssignment_ShouldRejectNonPdfFile_WithImageFormat() throws Exception {
        // Given
        MockMultipartFile imageFile = new MockMultipartFile(
                "file",
                "assignment.jpg",
                "image/jpeg",
                "JPEG image content".getBytes()
        );

        // When & Then
        mockMvc.perform(multipart("/api/assignments/{assignmentId}/submissions", assignment.getId())
                        .file(imageFile)
                        .header("Authorization", "Bearer " + studentToken)
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("File must be PDF format"))
                .andExpect(jsonPath("$.error").value("INVALID_FILE_TYPE"));
    }

    @Test
    void submitAssignment_ShouldRejectOversizedFile_ExceedingLimit() throws Exception {
        // Given - Create file just over 5MB (5,242,880 bytes)
        byte[] oversizedContent = new byte[5242881]; // 1 byte over limit
        MockMultipartFile oversizedFile = new MockMultipartFile(
                "file",
                "large_assignment.pdf",
                "application/pdf",
                oversizedContent
        );

        // When & Then
        mockMvc.perform(multipart("/api/assignments/{assignmentId}/submissions", assignment.getId())
                        .file(oversizedFile)
                        .header("Authorization", "Bearer " + studentToken)
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("File size exceeds 5MB limit"))
                .andExpect(jsonPath("$.error").value("FILE_TOO_LARGE"));
    }

    @Test
    void submitAssignment_ShouldAcceptMaxSizeFile_AtExactLimit() throws Exception {
        // Given - Create file exactly at 5MB limit (5,242,880 bytes)
        byte[] maxSizeContent = new byte[5242880]; // Exactly at limit
        MockMultipartFile maxSizeFile = new MockMultipartFile(
                "file",
                "max_size_assignment.pdf",
                "application/pdf",
                maxSizeContent
        );

        // When & Then
        mockMvc.perform(multipart("/api/assignments/{assignmentId}/submissions", assignment.getId())
                        .file(maxSizeFile)
                        .header("Authorization", "Bearer " + studentToken)
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fileName").value("max_size_assignment.pdf"));
    }

    @Test
    void submitAssignment_ShouldRejectEmptyFile() throws Exception {
        // Given
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty_assignment.pdf",
                "application/pdf",
                new byte[0] // Empty file
        );

        // When & Then
        mockMvc.perform(multipart("/api/assignments/{assignmentId}/submissions", assignment.getId())
                        .file(emptyFile)
                        .header("Authorization", "Bearer " + studentToken)
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("File cannot be empty"))
                .andExpect(jsonPath("$.error").value("EMPTY_FILE"));
    }

    @Test
    void submitAssignment_ShouldRejectMissingFile() throws Exception {
        // When & Then - No file in the request
        mockMvc.perform(multipart("/api/assignments/{assignmentId}/submissions", assignment.getId())
                        .header("Authorization", "Bearer " + studentToken)
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("File is required"))
                .andExpect(jsonPath("$.error").value("MISSING_FILE"));
    }

    @Test
    void submitAssignment_ShouldRejectFileWithSuspiciousExtension_ButPdfMimeType() throws Exception {
        // Given - File with PDF MIME type but executable extension
        MockMultipartFile suspiciousFile = new MockMultipartFile(
                "file",
                "assignment.exe", // Suspicious extension
                "application/pdf", // PDF MIME type
                "PDF content disguised as executable".getBytes()
        );

        // When & Then
        mockMvc.perform(multipart("/api/assignments/{assignmentId}/submissions", assignment.getId())
                        .file(suspiciousFile)
                        .header("Authorization", "Bearer " + studentToken)
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("File extension must be .pdf"))
                .andExpect(jsonPath("$.error").value("INVALID_FILE_EXTENSION"));
    }

    @Test
    void submitAssignment_ShouldAcceptValidPdfFile_WithCorrectMimeAndExtension() throws Exception {
        // Given
        MockMultipartFile validPdfFile = new MockMultipartFile(
                "file",
                "valid_assignment.pdf",
                "application/pdf",
                "Valid PDF content".getBytes()
        );

        // When & Then
        mockMvc.perform(multipart("/api/assignments/{assignmentId}/submissions", assignment.getId())
                        .file(validPdfFile)
                        .header("Authorization", "Bearer " + studentToken)
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fileName").value("valid_assignment.pdf"));
    }
}