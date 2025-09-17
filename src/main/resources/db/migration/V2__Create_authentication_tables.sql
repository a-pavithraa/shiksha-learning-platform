-- V2__Create_authentication_tables.sql
-- Create user management and authentication tables

-- User management and authentication
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    role user_role_enum NOT NULL, -- 'TEACHER' or 'STUDENT'
    grade_level INTEGER, -- For students (9,10,11,12), NULL for teachers
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE
);

-- Available subjects
CREATE TABLE subjects (
    id BIGSERIAL PRIMARY KEY,
    subject_name VARCHAR(50) UNIQUE NOT NULL, -- 'Math', 'Physics', 'Chemistry'
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE
);

-- Subject enrollment and teaching assignments
CREATE TABLE user_subjects (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    subject_id BIGINT NOT NULL REFERENCES subjects(id),
    grade_level INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, subject_id, grade_level)
);
