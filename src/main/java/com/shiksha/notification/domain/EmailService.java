package com.shiksha.notification.domain;

import org.springframework.stereotype.Service;

/**
 * Service for sending emails
 * Handles email composition and delivery
 */
@Service
public class EmailService {

    public void sendAssignmentNotification(String recipientEmail, String recipientName, 
                                         String assignmentTitle, String teacherName, 
                                         String subjectName, String dueDate) {
        
        // TODO: Implement actual email sending logic
        // This could use Spring Boot's mail starter or AWS SES
        
        System.out.printf("""
            📧 ASSIGNMENT NOTIFICATION EMAIL
            To: %s (%s)
            Subject: New Assignment: %s
            
            Dear %s,
            
            A new assignment has been posted for %s by %s.
            
            Assignment: %s
            Due Date: %s
            
            Please log in to the student portal to download and complete the assignment.
            
            Best regards,
            Shiksha LMS
            %n""", 
            recipientEmail, recipientName, assignmentTitle,
            recipientName, subjectName, teacherName, assignmentTitle, 
            dueDate != null ? dueDate : "No due date specified"
        );
    }

    public void sendEmail(String to, String subject, String content) {
        // Generic email sending method
        System.out.printf("""
            📧 EMAIL SENT
            To: %s
            Subject: %s
            Content: %s
            %n""", to, subject, content);
    }
}