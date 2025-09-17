package com.shiksha.notification.domain;

import com.shiksha.academic.domain.model.AssignmentCreatedEvent;
import com.shiksha.authentication.UserServiceAPI;

import com.shiksha.authentication.domain.models.StudentDto;
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

    private final UserServiceAPI userServiceAPI;
    private final EmailService emailService;

    public AssignmentCreatedEventListener(UserServiceAPI userServiceAPI, EmailService emailService) {
        this.userServiceAPI = userServiceAPI;
        this.emailService = emailService;
    }

    @ApplicationModuleListener
    @Async("applicationTaskExecutor")
    public void handleAssignmentCreated(AssignmentCreatedEvent event) {
        System.out.println("🎯 Processing AssignmentCreatedEvent for assignment: " + event.title());
        
        try {
            // Get all students enrolled in this subject and grade level
            List<StudentDto> students = userServiceAPI.getStudentsBySubjectAndGrade(
                    event.subjectId(), 
                    event.gradeLevel()
            );
            
            System.out.println("📋 Found " + students.size() + " students to notify");
            
            // Get teacher name for the email
            String teacherName = userServiceAPI.getUserFullName(event.teacherId());
            
            // Send notification to each student
            for (StudentDto student : students) {
                sendNotificationToStudent(student, event, teacherName);
            }
            
            System.out.println("✅ Notification processing completed for assignment: " + event.title());
            
        } catch (Exception e) {
            System.err.println("❌ Error processing assignment notification: " + e.getMessage());
            e.printStackTrace();
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