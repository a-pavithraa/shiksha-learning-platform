-- V6__Create_indexes.sql
-- Create database indexes for performance optimization

-- Authentication indexes
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_user_subjects_user_id ON user_subjects(user_id);
CREATE INDEX idx_user_subjects_subject_grade ON user_subjects(subject_id, grade_level);

-- Assignment indexes
CREATE INDEX idx_assignments_teacher_subject ON assignments(teacher_id, subject_id);
CREATE INDEX idx_assignments_grade_subject ON assignments(grade_level, subject_id);
CREATE INDEX idx_assignment_submissions_assignment ON assignment_submissions(assignment_id);
CREATE INDEX idx_assignment_submissions_student ON assignment_submissions(student_id);

-- Exam and grade indexes
CREATE INDEX idx_exams_teacher_subject ON exams(teacher_id, subject_id);
CREATE INDEX idx_exams_date_grade ON exams(exam_date, grade_level);
CREATE INDEX idx_grades_exam_student ON grades(exam_id, student_id);

-- Notification indexes
CREATE INDEX idx_notifications_recipient ON email_notifications(recipient_email);
CREATE INDEX idx_notifications_status ON email_notifications(status);
CREATE INDEX idx_notifications_type ON email_notifications(notification_type);