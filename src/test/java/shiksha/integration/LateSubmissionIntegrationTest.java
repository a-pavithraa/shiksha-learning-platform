package shiksha.integration;

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
class LateSubmissionIntegrationTest {

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

    private User student;
    private User teacher;
    private Assignment expiredAssignment;
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

        // Create assignment with past due date (expired)
        expiredAssignment = new Assignment(
                teacher.getId(),
                1L, // subjectId
                9, // gradeLevel
                "Late Math Homework",
                "Complete chapter 3 exercises",
                "/assignments/late-math-homework.pdf",
                "late-math-homework.pdf",
                LocalDate.now().minusDays(3) // Due 3 days ago
        );
        expiredAssignment = assignmentRepository.save(expiredAssignment);

        // Generate JWT token for student
        studentToken = tokenProvider.generateAccessToken(student);
    }

    @Test
    void submitAssignment_ShouldMarkAsLate_WhenSubmittedAfterDueDate() throws Exception {
        // Given
        MockMultipartFile pdfFile = new MockMultipartFile(
                "file",
                "late_assignment.pdf",
                "application/pdf",
                "Late assignment submission content".getBytes()
        );

        // When
        mockMvc.perform(multipart("/api/assignments/{assignmentId}/submissions", expiredAssignment.getId())
                        .file(pdfFile)
                        .header("Authorization", "Bearer " + studentToken)
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Late assignment submitted successfully"))
                .andExpect(jsonPath("$.data.fileName").value("late_assignment.pdf"))
                .andExpect(jsonPath("$.data.isLate").value(true))
                .andExpect(jsonPath("$.data.isResubmission").value(false));

        // Then
        Optional<AssignmentSubmission> submission = assignmentSubmissionRepository
                .findByAssignmentIdAndStudentId(expiredAssignment.getId(), student.getId());
        
        assertThat(submission).isPresent();
        assertThat(submission.get().getFileName()).isEqualTo("late_assignment.pdf");
        assertThat(submission.get().isLate()).isTrue();
        assertThat(submission.get().isResubmission()).isFalse();
    }

    @Test
    void submitAssignment_ShouldHandleNullDueDate_AsOnTime() throws Exception {
        // Given
        Assignment assignmentWithoutDueDate = new Assignment(
                teacher.getId(),
                1L, // subjectId
                9, // gradeLevel
                "No Due Date Assignment",
                "Assignment without due date",
                "/assignments/no-due-date.pdf",
                "no-due-date.pdf",
                null // No due date
        );
        assignmentWithoutDueDate = assignmentRepository.save(assignmentWithoutDueDate);

        MockMultipartFile pdfFile = new MockMultipartFile(
                "file",
                "no_due_date_submission.pdf",
                "application/pdf",
                "Submission for assignment without due date".getBytes()
        );

        // When
        mockMvc.perform(multipart("/api/assignments/{assignmentId}/submissions", assignmentWithoutDueDate.getId())
                        .file(pdfFile)
                        .header("Authorization", "Bearer " + studentToken)
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Assignment submitted successfully"))
                .andExpect(jsonPath("$.data.isLate").value(false));

        // Then
        Optional<AssignmentSubmission> submission = assignmentSubmissionRepository
                .findByAssignmentIdAndStudentId(assignmentWithoutDueDate.getId(), student.getId());
        
        assertThat(submission).isPresent();
        assertThat(submission.get().isLate()).isFalse();
    }

    @Test
    void submitAssignment_ShouldTriggerLateNotification_WhenLateSubmission() throws Exception {
        // Given
        MockMultipartFile pdfFile = new MockMultipartFile(
                "file",
                "notification_test.pdf",
                "application/pdf",
                "Test late notification".getBytes()
        );

        // When
        mockMvc.perform(multipart("/api/assignments/{assignmentId}/submissions", expiredAssignment.getId())
                        .file(pdfFile)
                        .header("Authorization", "Bearer " + studentToken)
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.isLate").value(true));

        // Then
        // Verify that the submission was created with late flag
        Optional<AssignmentSubmission> submission = assignmentSubmissionRepository
                .findByAssignmentIdAndStudentId(expiredAssignment.getId(), student.getId());
        
        assertThat(submission).isPresent();
        assertThat(submission.get().isLate()).isTrue();
        
        // Note: Notification verification would be done through event publication
        // testing in the event listener tests
    }
}