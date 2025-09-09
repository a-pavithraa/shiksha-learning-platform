-- V5__Create_notification_tables.sql
-- Create notification module tables for email notifications

-- Email notification tracking
CREATE TABLE email_notifications (
    id BIGSERIAL PRIMARY KEY,
    recipient_email VARCHAR(255) NOT NULL,
    recipient_id BIGINT REFERENCES users(id),
    subject VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    notification_type notification_type_enum NOT NULL,
    related_entity_id BIGINT, -- ID of assignment, exam, or grade
    related_entity_type VARCHAR(50), -- 'ASSIGNMENT', 'EXAM', 'GRADE'
    sent_at TIMESTAMP,
    status notification_status_enum DEFAULT 'PENDING',
    error_message TEXT,
    retry_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);