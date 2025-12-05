package shiksha.academic.domain;

import com.shiksha.academic.domain.*;
import com.shiksha.academic.domain.command.SubmitAssignmentCommand;
import com.shiksha.academic.domain.dto.AssignmentSubmissionDto;
import com.shiksha.academic.domain.model.AssignmentSubmissionCreatedEvent;
import com.shiksha.authentication.UserServiceAPI;
import com.shiksha.authentication.domain.User;
import com.shiksha.authentication.domain.UserRepository;
import com.shiksha.authentication.domain.UserRole;
import com.shiksha.authentication.domain.models.UserDto;
import com.shiksha.common.S3FileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class AssignmentSubmissionServiceTest {

    @Mock
    private AssignmentSubmissionRepository submissionRepository;

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private S3FileService s3FileService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private AssignmentSubmissionService service;
    @Mock
    private UserServiceAPI userServiceAPI;
    @Mock
    private AssignmentSubmission assignmentSubmission;

    private Assignment assignment;
    private User student;
    private User teacher;
    private MockMultipartFile testFile;

    @BeforeEach
    void setUp() {
        // Create test assignment
        assignment = mock(Assignment.class);
        when(assignment.getId()).thenReturn(1L);
        when(assignment.getTeacherId()).thenReturn(2L);
        when(assignment.getTitle()).thenReturn("Test Assignment");
        when(assignment.getDueDate()).thenReturn(LocalDate.now().plusDays(7));
        when(assignment.isActive()).thenReturn(true);

        // Create test student
        student = mock(User.class);
        when(student.getId()).thenReturn(3L);
        when(student.getFirstName()).thenReturn("John");
        when(student.getLastName()).thenReturn("Doe");
        when(student.getRole()).thenReturn(UserRole.STUDENT);

        // Create test teacher
        teacher = mock(User.class);
        when(teacher.getId()).thenReturn(2L);

        // Create test file
        testFile = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "Test PDF content".getBytes()
        );
    }

    @Test
    void submitAssignment_ShouldSucceed_WhenValidSubmission() {
        // Given
        SubmitAssignmentCommand command = SubmitAssignmentCommand.create(1L, 1L, testFile);
        
        when(userServiceAPI.findById(1L)).thenReturn(Optional.of(new UserDto(1L,"test@test.com","John","Doe")));
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(new Assignment()));
        when(submissionRepository.findByAssignmentIdAndStudentId(1L, 1L)).thenReturn(Optional.of(assignmentSubmission));
        when(s3FileService.uploadFile(anyString(), any())).thenReturn("s3://bucket/path");
        
        AssignmentSubmission savedSubmission = new AssignmentSubmission(1L, 1L, "s3://bucket/path",
                "test.pdf", (long) testFile.getSize(), false, false);
        when(submissionRepository.save(any(AssignmentSubmission.class))).thenReturn(savedSubmission);

        // When
        AssignmentSubmissionDto result = service.submitAssignment(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.fileName()).isEqualTo("test.pdf");
        assertThat(result.isLate()).isFalse();
        assertThat(result.isResubmission()).isFalse();

        verify(s3FileService).uploadFile(anyString(), eq(testFile));
        verify(submissionRepository).save(any(AssignmentSubmission.class));
        verify(eventPublisher).publishEvent(any(AssignmentSubmissionCreatedEvent.class));
    }

    @Test
    void submitAssignment_ShouldThrowException_WhenAssignmentNotFound() {
        // Given
        SubmitAssignmentCommand command = SubmitAssignmentCommand.create(999L, 3L, testFile);
        when(assignmentRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> service.submitAssignment(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Assignment not found");
    }

    @Test
    void submitAssignment_ShouldThrowException_WhenStudentNotFound() {
        // Given
        SubmitAssignmentCommand command = SubmitAssignmentCommand.create(1L, 999L, testFile);
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> service.submitAssignment(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Student not found");
    }
}