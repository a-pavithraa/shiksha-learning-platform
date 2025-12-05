package com.shiksha.notification.domain;

import com.shiksha.notification.domain.AssignmentSubmissionEventListener.NotificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Basic implementation of NotificationService for email notifications.
 * In a real application, this would integrate with email service providers.
 */
@Service
public class EmailNotificationService implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationService.class);

    @Override
    public void sendNotification(NotificationRequest request) {
        try {
            // Log the notification (in real implementation, this would send actual email)
            logger.info("Sending email notification: " +
                       "to={}, subject={}, type={}, priority={}, relatedEntityId={}", 
                       request.email(), request.subject(), request.type(), 
                       request.priority(), request.relatedEntityId());
            
            // Simulate email sending delay
            Thread.sleep(100);
            
            logger.info("Email notification sent successfully to {}", request.email());
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Email notification interrupted for {}", request.email(), e);
        } catch (Exception e) {
            logger.error("Failed to send email notification to {}: {}", request.email(), e.getMessage(), e);
            // In a real implementation, you might want to queue for retry
        }
    }
}