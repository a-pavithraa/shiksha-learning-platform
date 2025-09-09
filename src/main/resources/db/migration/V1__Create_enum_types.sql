-- V1__Create_enum_types.sql
-- Create ENUM types for user roles, submission status, and notification types

-- Create ENUM type for user roles
CREATE TYPE user_role_enum AS ENUM ('TEACHER', 'STUDENT','ADMIN');

-- Create ENUM for assignment submission status
CREATE TYPE submission_status_enum AS ENUM ('SUBMITTED', 'REVIEWED', 'GRADED');

-- Create ENUMs for notifications
CREATE TYPE notification_type_enum AS ENUM (
    'ASSIGNMENT_POSTED', 
    'ASSIGNMENT_SUBMITTED', 
    'EXAM_SCHEDULED', 
    'GRADE_POSTED',
    'SYSTEM_NOTIFICATION'
);

CREATE TYPE notification_status_enum AS ENUM ('PENDING', 'SENT', 'FAILED', 'RETRYING');