-- V4__Create_examination_tables.sql
-- Create examination module tables for exams and grades

-- Scheduled examinations
CREATE TABLE exams (
    id BIGSERIAL PRIMARY KEY,
    teacher_id BIGINT NOT NULL REFERENCES users(id),
    subject_id BIGINT NOT NULL REFERENCES subjects(id),
    grade_level INTEGER NOT NULL CHECK (grade_level BETWEEN 9 AND 12),
    exam_title VARCHAR(255) NOT NULL,
    exam_date DATE NOT NULL,
    exam_time TIME NOT NULL,
    duration_minutes INTEGER NOT NULL,
    topics TEXT NOT NULL,
    special_instructions TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE
);

-- Exam results/grades
CREATE TABLE grades (
    id BIGSERIAL PRIMARY KEY,
    exam_id BIGINT NOT NULL REFERENCES exams(id),
    student_id BIGINT NOT NULL REFERENCES users(id),
    total_score DECIMAL(5,2) NOT NULL,
    max_score DECIMAL(5,2) NOT NULL,
    graded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    graded_by BIGINT NOT NULL REFERENCES users(id), -- teacher who graded
    comments TEXT,
    UNIQUE(exam_id, student_id)
);