package com.shiksha.notification.domain;

import com.shiksha.notification.domain.AssignmentSubmissionEventListener.NotificationRequest;

public interface NotificationService {
    void sendNotification(NotificationRequest request);
}