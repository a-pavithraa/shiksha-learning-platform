-- V3__Create_academic_tables.sql
-- Create academic module tables for assignments and submissions

-- Teacher-created assignments (questions/problems)
CREATE TABLE assignments (
    id BIGSERIAL PRIMARY KEY,
    teacher_id BIGINT NOT NULL REFERENCES users(id),
    subject_id BIGINT NOT NULL REFERENCES subjects(id),
    grade_level INTEGER NOT NULL CHECK (grade_level BETWEEN 9 AND 12),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    file_path VARCHAR(500) NOT NULL, -- S3 object key
    file_name VARCHAR(255) NOT NULL,
    due_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE
);

-- Student completed assignment submissions
CREATE TABLE assignment_submissions (
    id BIGSERIAL PRIMARY KEY,
    assignment_id BIGINT NOT NULL REFERENCES assignments(id),
    student_id BIGINT NOT NULL REFERENCES users(id),
    file_path VARCHAR(500) NOT NULL, -- S3 object key
    file_name VARCHAR(255) NOT NULL,
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status submission_status_enum DEFAULT 'SUBMITTED',
    teacher_feedback TEXT,
    UNIQUE(assignment_id, student_id)
);