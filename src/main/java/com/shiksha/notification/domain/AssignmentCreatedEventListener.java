package com.shiksha.notification.domain;

import com.shiksha.academic.domain.model.AssignmentCreatedEvent;
import com.shiksha.authentication.UserServiceAPI;

import com.shiksha.authentication.domain.User;
import com.shiksha.authentication.domain.models.StudentDto;
import com.shiksha.authentication.domain.models.UserDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Event listener for AssignmentCreatedEvent
 * Handles sending email notifications to students when a new assignment is created
 */
@Component
public class AssignmentCreatedEventListener {

    private static final Logger logger = LoggerFactory.getLogger(AssignmentCreatedEventListener.class);
    private final UserServiceAPI userServiceAPI;
    private final EmailService emailService;

    public AssignmentCreatedEventListener(UserServiceAPI userServiceAPI, EmailService emailService) {
        this.userServiceAPI = userServiceAPI;
        this.emailService = emailService;
    }

    @ApplicationModuleListener
    public void handleAssignmentCreated(AssignmentCreatedEvent event) {
        System.out.println("🎯 Processing AssignmentCreatedEvent for assignment: " + event.title());
        
        try {
            // Get all students enrolled in this subject and grade level
            List<StudentDto> students = userServiceAPI.getStudentsBySubjectAndGrade(
                    event.subjectId(), 
                    event.gradeLevel()
            );
            
            System.out.println("📋 Found " + students.size() + " students to notify");
            
            // Get teacher name for the email(event.teacherId());
            UserDto teacher = userServiceAPI.findById(event.teacherId()).orElseThrow(() -> new IllegalArgumentException("Teacher not found"));
            String teacherName = teacher.firstName() + " " + teacher.lastName();
            
            // Send notification to each student
            for (StudentDto student : students) {
                sendNotificationToStudent(student, event, teacherName);
            }
            
            System.out.println("✅ Notification processing completed for assignment: " + event.title());
            
        } catch (Exception e) {
            logger.error("❌ Error processing assignment notification for assignment: {}", event.title(), e);
        }
    }

    private void sendNotificationToStudent(StudentDto student, AssignmentCreatedEvent event, String teacherName) {
        String dueDate = event.dueDate() != null ? event.dueDate().toString() : null;
        
        emailService.sendAssignmentNotification(
                student.getEmail(),
                student.getFullName(),
                event.title(),
                teacherName,
                "Subject-" + event.subjectId(), // TODO: Get actual subject name
                dueDate
        );
    }
}