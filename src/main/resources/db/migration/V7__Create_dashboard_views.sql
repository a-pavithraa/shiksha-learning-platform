-- V7__Create_dashboard_views.sql
-- Create database views for dashboard analytics

-- Teacher Dashboard: Student Performance Summary
CREATE VIEW teacher_student_summary AS
SELECT 
    u.id as student_id,
    u.first_name,
    u.last_name,
    u.grade_level,
    s.subject_name,
    COUNT(a.id) as total_assignments,
    COUNT(asub.id) as submitted_assignments,
    ROUND(AVG(g.total_score), 2) as average_grade,
    COUNT(g.id) as total_exams_graded
FROM users u
LEFT JOIN user_subjects us ON u.id = us.user_id
LEFT JOIN subjects s ON us.subject_id = s.id
LEFT JOIN assignments a ON s.id = a.subject_id AND u.grade_level = a.grade_level
LEFT JOIN assignment_submissions asub ON a.id = asub.assignment_id AND u.id = asub.student_id
LEFT JOIN grades g ON u.id = g.student_id
WHERE u.role = 'STUDENT'
GROUP BY u.id, u.first_name, u.last_name, u.grade_level, s.subject_name;

-- Student Dashboard: Subject-wise Performance
CREATE VIEW student_subject_dashboard AS
SELECT 
    u.id as student_id,
    s.subject_name,
    COUNT(DISTINCT a.id) as total_assignments,
    COUNT(DISTINCT asub.id) as completed_assignments,
    COUNT(DISTINCT CASE WHEN asub.id IS NULL THEN a.id END) as pending_assignments,
    COUNT(DISTINCT g.id) as total_grades,
    ROUND(AVG(g.total_score), 2) as average_score,
    COUNT(DISTINCT CASE WHEN e.exam_date > CURRENT_DATE THEN e.id END) as upcoming_exams
FROM users u
JOIN user_subjects us ON u.id = us.user_id
JOIN subjects s ON us.subject_id = s.id
LEFT JOIN assignments a ON s.id = a.subject_id AND u.grade_level = a.grade_level
LEFT JOIN assignment_submissions asub ON a.id = asub.assignment_id AND u.id = asub.student_id
LEFT JOIN exams e ON s.id = e.subject_id AND u.grade_level = e.grade_level
LEFT JOIN grades g ON e.id = g.exam_id AND u.id = g.student_id
WHERE u.role = 'STUDENT'
GROUP BY u.id, s.subject_name;