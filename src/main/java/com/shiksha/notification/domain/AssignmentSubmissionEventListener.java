package com.shiksha.notification.domain;

import com.shiksha.academic.domain.model.AssignmentSubmissionCreatedEvent;
import com.shiksha.authentication.UserServiceAPI;
import com.shiksha.authentication.domain.models.UserDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;

@Component
@Transactional
public class AssignmentSubmissionEventListener {

    private static final Logger logger = LoggerFactory.getLogger(AssignmentSubmissionEventListener.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm");

    private final UserServiceAPI userServiceAPI;
    private final NotificationService notificationService;

    public AssignmentSubmissionEventListener(UserServiceAPI userServiceAPI,
                                           NotificationService notificationService) {
        this.userServiceAPI = userServiceAPI;
        this.notificationService = notificationService;
    }

    /**
     * Handle assignment submission created events
     */
    @ApplicationModuleListener
    public void handleAssignmentSubmissionCreated(AssignmentSubmissionCreatedEvent event) {
        logger.info("Processing assignment submission event: submissionId={}, assignmentId={}, studentId={}, isLate={}, isResubmission={}",
                   event.submissionId(), event.assignmentId(), event.studentId(), 
                   event.isLate(), event.isResubmission());

        try {
            // Send notification to teacher
            sendTeacherNotification(event);
            
            // Send confirmation to student if late submission
            if (event.isLate()) {
                sendStudentLateSubmissionNotification(event);
            }

            logger.info("Successfully processed assignment submission event: submissionId={}", event.submissionId());

        } catch (Exception e) {
            logger.error("Failed to process assignment submission event: submissionId={}, error={}", 
                        event.submissionId(), e.getMessage(), e);
            // Don't rethrow - we don't want to rollback the submission transaction
        }
    }

    private void sendTeacherNotification(AssignmentSubmissionCreatedEvent event) {
        if (event.teacherId() == null) {
            logger.warn("No teacher ID in submission event, skipping teacher notification: submissionId={}", 
                       event.submissionId());
            return;
        }

        UserDto teacher = userServiceAPI.findById(event.teacherId()).orElseThrow(() -> new IllegalArgumentException("Teacher not found"));
        if (teacher == null) {
            logger.warn("Teacher not found for notification: teacherId={}, submissionId={}", 
                       event.teacherId(), event.submissionId());
            return;
        }

        // Create notification for teacher
        String subject = createTeacherNotificationSubject(event);
        String message = createTeacherNotificationMessage(event);
        
        NotificationRequest teacherNotification = new NotificationRequest(
                teacher.id(),
                teacher.email(),
                subject,
                message,
                NotificationType.ASSIGNMENT_SUBMISSION,
                event.getPriority().name(),
                event.assignmentId()
        );

        notificationService.sendNotification(teacherNotification);
        
        logger.info("Teacher notification sent: teacherId={}, submissionId={}", 
                   event.teacherId(), event.submissionId());
    }

    private void sendStudentLateSubmissionNotification(AssignmentSubmissionCreatedEvent event) {
        UserDto student = userServiceAPI.findById(event.studentId()).orElseThrow(() -> new IllegalArgumentException("Student not found"));
        if (student == null) {
            logger.warn("Student not found for late submission notification: studentId={}, submissionId={}", 
                       event.studentId(), event.submissionId());
            return;
        }

        // Create late submission confirmation for student
        String subject = "Late Assignment Submission Confirmation";
        String message = createStudentLateNotificationMessage(event);
        
        NotificationRequest studentNotification = new NotificationRequest(
                student.id(),
                student.email(),
                subject,
                message,
                NotificationType.SUBMISSION_CONFIRMATION,
                "MEDIUM",
                event.assignmentId()
        );

        notificationService.sendNotification(studentNotification);
        
        logger.info("Student late submission notification sent: studentId={}, submissionId={}", 
                   event.studentId(), event.submissionId());
    }

    private String createTeacherNotificationSubject(AssignmentSubmissionCreatedEvent event) {
        StringBuilder subject = new StringBuilder();
        
        if (event.isResubmission()) {
            subject.append("RESUBMISSION: ");
        } else if (event.isLate()) {
            subject.append("LATE SUBMISSION: ");
        } else {
            subject.append("New Assignment Submission: ");
        }
        
        subject.append(event.assignmentTitle());
        
        return subject.toString();
    }

    private String createTeacherNotificationMessage(AssignmentSubmissionCreatedEvent event) {
        StringBuilder message = new StringBuilder();
        
        message.append("Student ").append(event.studentName())
               .append(" has ");
        
        if (event.isResubmission()) {
            message.append("resubmitted");
        } else {
            message.append("submitted");
        }
        
        message.append(" the assignment '").append(event.assignmentTitle()).append("'");
        
        if (event.isLate()) {
            message.append(" (LATE SUBMISSION)");
        }
        
        message.append(".\n\n")
               .append("File: ").append(event.fileName()).append("\n")
               .append("Submitted: ").append(event.submissionTimestamp().format(DATE_FORMATTER)).append("\n\n")
               .append("Please review the submission in the teacher dashboard.");
        
        return message.toString();
    }

    private String createStudentLateNotificationMessage(AssignmentSubmissionCreatedEvent event) {
        return String.format(
                "Your assignment submission for '%s' has been received.\n\n" +
                "Please note: This submission was made after the due date and is marked as LATE.\n\n" +
                "File submitted: %s\n" +
                "Submission time: %s\n\n" +
                "Your teacher will be notified about this late submission.",
                event.assignmentTitle(),
                event.fileName(),
                event.submissionTimestamp().format(DATE_FORMATTER)
        );
    }

    // Notification-related classes (these would typically be in separate files)
    
    public record NotificationRequest(
            Long userId,
            String email,
            String subject,
            String message,
            NotificationType type,
            String priority,
            Long relatedEntityId
    ) {}

    public enum NotificationType {
        ASSIGNMENT_SUBMISSION,
        SUBMISSION_CONFIRMATION,
        ASSIGNMENT_GRADED,
        ASSIGNMENT_REMINDER
    }


}