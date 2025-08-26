# Shiksha Tuition Center App - Product Requirements Document (PRD)

## Executive Summary

**Product Vision:** Create a streamlined web application for Shiksha tuition center that enhances communication between teachers and students through efficient assignment sharing, exam scheduling, and assessment management.

**Bottom Line Up Front:** A responsive web application that allows 3 teachers to share assignments and schedule exams for 40 students across Math, Physics, and Chemistry subjects (grades 9-12), with automated email notifications and secure PDF file handling.

---

## 1. Project Overview

### Product Name
Shiksha Learning Management System

### Problem Statement
Shiksha tuition center currently lacks a centralized digital platform for:
- Teachers to efficiently share assignments and schedule exams
- Students to submit assessments and receive timely updates
- Automated notification system for assignment uploads and exam scheduling
- Centralized grade management and distribution

### Target Users
- **Primary:** 3 subject teachers (Math, Physics, Chemistry)
- **Primary:** 40 students (grades 9-12)
- **Secondary:** Potential future expansion to more teachers/students

### Success Metrics
- 100% teacher adoption for assignment sharing within 2 weeks
- 90% student engagement with uploaded assignments
- 95% successful email notification delivery rate
- Average assessment submission time reduced by 60%
- Zero security incidents related to file uploads

---

## 2. Core Features & User Stories

### 2.1 User Authentication & Management
**Must-Have**
- **Teacher Registration:** As a teacher, I want to register with my subject specialization so I can access subject-specific classes
- **Student Registration:** As a student, I want to register and select my subjects so I receive relevant assignments
- **Role-Based Access:** As a system admin, I want different permission levels for teachers vs students

### 2.2 Assignment Management
**Must-Have**
- **Assignment Upload:** As a teacher, I want to upload PDF assignments so students can access them anytime
- **Assignment Organization:** As a teacher, I want to categorize assignments by subject and grade level
- **Assignment Access:** As a student, I want to view all assignments for my enrolled subjects
- **Email Notifications:** As a student, I want to receive email notifications when new assignments are posted

### 2.3 Assessment Submission
**Must-Have**
- **Assessment Upload:** As a student, I want to upload completed assessments as PDF files
- **Submission Tracking:** As a teacher, I want to see which students have submitted assessments
- **Teacher Notifications:** As a teacher, I want email notifications when students submit assessments
- **File Security:** As a user, I want assurance that uploaded files are secure and properly validated

### 2.4 Exam Scheduling
**Must-Have**
- **Exam Creation:** As a teacher, I want to schedule exams with date, time, duration, syllabus topics, and special instructions
- **Exam Visibility:** As a student, I want to view upcoming exams for all my subjects
- **Schedule Notifications:** As a student, I want email notifications for newly scheduled exams

### 2.5 Grade Management
**Must-Have**
- **Grade Entry:** As a teacher, I want to upload total scores for completed exams
- **Grade Visibility:** As a student, I want to view my exam results
- **Grade Notifications:** As a student, I want email notifications when grades are posted

---

## 3. Technical Requirements

### 3.1 Recommended Technology Stack

Based on current best practices for educational platforms and your requirements:

**Frontend:**
- **React 18+** with TypeScript for type safety and better developer experience
- **React Router DOM** for client-side routing
- **Tailwind CSS** for rapid, responsive UI development
- **React Hook Form + Zod** for robust form handling and validation
- **Axios** for API communication

**Backend:**
- **Java 21** with **Spring Boot 3.x**
- **Spring Modulith** for modular architecture and better domain separation
- **Spring Security** with JWT tokens for authentication and authorization
- **Spring Data JPA** for database operations
- **Spring Web** for REST API development
- **AWS SDK for Java** with **Amazon SES** for email notifications
- **Spring Cloud AWS SQS** for reliable notification queuing
- **Spring Boot File Upload** for secure PDF handling

**Database & Storage:**
- **PostgreSQL** for structured data (users, assignments, grades, schedules)
- **AWS S3** for secure PDF file storage and React app static files
- **AWS CloudFront** for global content delivery and frontend hosting

**Infrastructure:**
- **AWS S3 + CloudFront** for React frontend hosting with global CDN
- **AWS EC2** or **AWS Elastic Beanstalk** for Spring Boot application hosting
- **AWS SQS** for reliable notification message queuing
- **Amazon SES** for email notifications
- **AWS RDS PostgreSQL** for managed database hosting

### 3.2 Security Requirements

Given the file upload functionality, security is critical:

- **File Validation:** Restrict file uploads to PDF only, validate MIME types and file signatures
- **File Storage:** Store files in AWS S3 with proper access controls and randomized names
- **Access Control:** Role-based authentication with JWT tokens
- **Input Sanitization:** Validate all form inputs and file names
- **File Size Limits:** Maximum 10MB per file upload
- **HTTPS Only:** All communications encrypted in transit via CloudFront

### 3.3 Database Schema

The PostgreSQL database will maintain the following core tables:

**Authentication & User Management:**
- **`users`** - Basic user information (id, email, password_hash, first_name, last_name, phone, created_at, updated_at, is_active)
- **`roles`** - User roles (id, role_name: 'TEACHER', 'STUDENT', 'ADMIN')
- **`user_roles`** - Many-to-many relationship between users and roles
- **`user_subjects`** - Students enrolled in subjects, Teachers assigned to subjects (user_id, subject_id, grade_level)

**Core Application Data:**
- **`subjects`** - Available subjects (id, subject_name: 'Math', 'Physics', 'Chemistry')
- **`assignments`** - Teacher-created assignments (id, teacher_id, subject_id, grade_level, title, description, file_path, due_date, created_at)
- **`assessments`** - Student submissions (id, assignment_id, student_id, file_path, submitted_at, status)
- **`exams`** - Scheduled examinations (id, teacher_id, subject_id, grade_level, exam_date, duration, topics, instructions, created_at)
- **`grades`** - Exam results (id, exam_id, student_id, total_score, max_score, graded_at, graded_by)

**Notification Tracking:**
- **`email_notifications`** - Notification history (id, recipient_email, subject, content, sent_at, status, notification_type)

### 3.4 Security Implementation Details

**JWT Token Management:**
- Tokens contain user ID, role, and subject permissions
- Token expiration: 24 hours for regular sessions
- Refresh token mechanism for seamless user experience
- Secure token storage in HTTP-only cookies (frontend) and memory (backend)

**Database Security:**
- Encrypted passwords using BCrypt hashing
- Prepared statements to prevent SQL injection
- Row-level security for student data access
- Audit trails for sensitive operations (grade changes, file uploads)

### 3.5 Performance Requirements

- **Load Time:** Pages load within 3 seconds on 3G connections (CloudFront optimization)
- **Concurrent Users:** Support 50 simultaneous users (peak usage scenario)
- **File Upload:** Support multiple 10MB PDF uploads simultaneously to S3
- **Email Delivery:** SES notification delivery within 200ms of trigger event
- **Global Performance:** CloudFront CDN ensures fast loading worldwide

---

## 4. User Experience Design

### 4.1 Core User Flows

**Teacher Workflow:**
1. Login → Dashboard → Select Subject/Grade → Upload Assignment → Students notified
2. Login → Exam Schedule → Set details → Students notified
3. Login → Grade Entry → Select exam → Enter scores → Students notified

**Student Workflow:**
1. Login → Dashboard → View assignments by subject
2. Download assignment → Complete → Upload assessment → Teacher notified
3. View exam schedule → Prepare → Check grades after completion

### 4.2 Interface Requirements

- **Responsive Design:** Mobile-first approach for student accessibility
- **Grade-Level Organization:** Clear subject-based navigation
- **File Upload UX:** Drag-and-drop interface with progress indicators
- **Notification Center:** In-app notification history alongside email notifications
- **Dashboard:** Quick overview of pending assignments, upcoming exams, recent grades

### 4.3 Accessibility Considerations

- **Keyboard Navigation:** All features accessible via keyboard
- **Screen Reader Support:** Proper semantic HTML and ARIA labels
- **Color Contrast:** WCAG 2.1 AA compliance
- **Mobile Optimization:** Touch-friendly interface for phone users

---

## 5. Functional Specifications

### 5.1 User Roles & Permissions

| Feature | Teacher | Student |
|---------|---------|---------|
| Upload Assignments | ✅ | ❌ |
| Download Assignments | ✅ | ✅ |
| Upload Assessments | ❌ | ✅ |
| Download Assessments | ✅ | ❌ |
| Schedule Exams | ✅ | ❌ |
| View Exam Schedule | ✅ | ✅ |
| Enter Grades | ✅ | ❌ |
| View Own Grades | ❌ | ✅ |

### 5.2 Notification System

**Email Notifications Include:**

*For Students:*
- New assignment posted (subject, due date, teacher name)
- Exam scheduled (date, time, duration, topics, instructions)
- Grades posted (exam name, score, total marks)

*For Teachers:*
- Student assessment submitted (student name, assignment title, submission time)
- System updates and maintenance notifications

### 5.3 File Management

- **Supported Format:** PDF only
- **Storage:** AWS S3 with secure access controls
- **Naming Convention:** Auto-generated secure filenames
- **Storage Organization:** Organized by subject → grade → type (assignment/assessment)
- **Access Control:** Students can only access their grade-level content

---

## 6. Development Considerations

### 6.1 Technical Architecture

```
CloudFront CDN (Global)
    ↓ Static files (React App)
AWS S3 Bucket (Frontend + PDF Storage)
    ↑ Upload/Download PDF files
React Frontend (TypeScript)
    ↓ REST API calls
AWS Load Balancer/EC2 (Spring Boot App)
    ├── User Management Module
    ├── Assignment Module
    ├── Assessment Module
    ├── Exam Scheduling Module
    └── Notification Module
            ↓ Queue messages
        AWS SQS (Notification Queue)
            ↓ Process messages
        SQS Consumer → Amazon SES
    ↓ Database queries
AWS RDS (PostgreSQL)
```

**Enhanced Architecture with SQS Benefits:**
- **Reliable Notifications:** SQS ensures email notifications are never lost
- **Asynchronous Processing:** User actions complete instantly, emails sent in background
- **Fault Tolerance:** If SES fails, messages remain in queue for retry
- **Scalability:** Handle notification bursts during peak times (exam announcements)
- **Monitoring:** CloudWatch integration for queue metrics and alerting
- **Dead Letter Queue:** Failed notifications moved to DLQ for investigation

### 6.2 Development Phases

**Phase 1 (MVP - 4 weeks):**
- Set up AWS infrastructure (S3, CloudFront, RDS PostgreSQL)
- Create Spring Boot project with Spring Modulith structure
- Implement user authentication with Spring Security and JWT
- Set up React frontend with TypeScript and deploy to S3/CloudFront
- Configure Amazon SES for email notifications

**Phase 2 (Enhancement - 3 weeks):**
- Develop assignment management APIs and React components
- Implement secure PDF upload to S3 with proper access controls
- Build student assessment submission workflow
- Create basic grade management functionality with SES integration

**Phase 3 (Polish - 2 weeks):**
- Complete exam scheduling module
- Enhanced email notification templates for SES
- Mobile responsiveness optimization and CloudFront caching
- Integration testing across all Spring Modulith modules

**Phase 4 (Testing & Deployment - 1 week):**
- User acceptance testing with teachers and students
- Performance optimization and CloudFront cache configuration
- Production deployment with proper AWS security policies
- Monitoring setup with CloudWatch and documentation completion

### 6.3 Risk Assessment & Mitigation

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| File upload vulnerabilities | High | Medium | Implement strict file validation, store in S3 with proper access controls |
| Amazon SES delivery failures | Medium | Low | Use SES with proper error handling, implement retry logic |
| Data loss | High | Low | Regular automated backups, version control |
| Poor mobile experience | Medium | Medium | Mobile-first responsive design, thorough testing |
| MCP server integration complexity (future) | Medium | Medium | Plan for proper API design, comprehensive testing |

---

## 7. Implementation Timeline

### Week 1-2: AWS Infrastructure Setup
- Create and configure S3 buckets for frontend and file storage
- Set up CloudFront distribution with Origin Access Control
- Configure RDS PostgreSQL instance
- Set up Amazon SES for email notifications
- Create IAM roles and policies for secure access

### Week 3-4: Backend Development
- Develop Spring Boot application with Spring Modulith
- Implement authentication APIs with Spring Security
- Create assignment, assessment, and exam management modules
- Set up email notification service with SES integration

### Week 5-6: Frontend Development
- Build React components for teacher and student dashboards
- Implement file upload/download functionality
- Create responsive UI with Tailwind CSS
- Integrate with backend APIs

### Week 7-8: Integration & Testing
- End-to-end testing of all workflows
- Performance optimization
- Security testing and validation
- User acceptance testing with sample teachers and students

### Week 9: Production Deployment
- Deploy to production AWS environment
- Set up monitoring and logging
- Train teachers on the system
- Go live with gradual rollout

---

## 8. Success Measurement

### 8.1 Key Performance Indicators

**User Engagement:**
- Daily active users (target: 80% of total users)
- Assignment download rate (target: 95% within 24 hours)
- Assessment submission rate (target: 90% on-time submissions)

**System Performance:**
- Page load times (target: <3 seconds via CloudFront)
- Email delivery success rate (target: 95%+ via SES)
- File upload success rate (target: 99%+ to S3)

**User Satisfaction:**
- Teacher satisfaction with workflow efficiency (target: 4.5/5)
- Student satisfaction with accessibility (target: 4.5/5)

### 8.2 Launch Criteria

- All core user stories implemented and tested
- Security audit completed with no critical issues
- Email notification system tested with 100% delivery success
- Mobile responsiveness validated across devices
- User training completed for all teachers
- AWS infrastructure properly configured and secured

---

## 9. Future Enhancements (Post-Launch)

### 9.1 Priority Enhancements

**AI-Powered Student Support Chatbot**
- **Purpose:** Automated responses to student queries about marks, assessment schedules, and exam dates
- **Technical Implementation:** 
  - Integration with **MCP (Model Context Protocol) database server** for real-time data access
  - **Large Language Model** (Claude, GPT-4, or local model) for natural language processing
  - **Spring Boot integration** via REST API endpoints for chatbot backend services
  - **Database queries** through MCP server to fetch student-specific information
- **Key Features:**
  - Query marks: "What's my Physics exam score?"
  - Check schedules: "When is my next Math assignment due?"
  - Exam information: "What topics are covered in tomorrow's Chemistry exam?"
  - Assessment status: "Have I submitted my Physics assessment?"
- **Security:** Student authentication required, access only to own data
- **Fallback:** Direct teacher contact for queries the chatbot cannot handle

### 9.2 Additional Future Features

- **Analytics Dashboard:** Track student progress and engagement metrics
- **Advanced Grading:** Support for rubrics and detailed feedback comments
- **Parent Portal:** Optional parent access to student progress and notifications
- **Mobile App:** Native iOS/Android apps for enhanced mobile experience
- **Integration:** Connect with existing school management systems
- **Offline Support:** Download assignments for offline viewing
- **Advanced Search:** Full-text search across assignments and announcements
- **Calendar Integration:** Sync exam schedules with student calendars

---

## 10. Technical Documentation Requirements

- API documentation for all Spring Boot endpoints
- Database schema documentation with entity relationships
- AWS infrastructure setup guide (S3, CloudFront, RDS, SES)
- Security implementation guide including JWT handling
- Deployment and maintenance procedures
- User training materials for teachers and students
- MCP server integration guide (for future chatbot enhancement)

---

## 11. Cost Estimation

### Development Costs
- **Development Time:** 9 weeks for full implementation
- **AWS Setup:** Initial infrastructure configuration
- **Testing & QA:** Comprehensive testing across all modules

### Monthly Operating Costs (Estimated)
- **AWS RDS PostgreSQL:** ~$15-25/month (t3.micro instance)
- **AWS S3 Storage:** ~$1-3/month (file storage)
- **CloudFront CDN:** ~$1-5/month (data transfer)
- **Amazon SES:** ~$0.10/month (very low email volume)
- **AWS SQS:** ~$0.50/month (notification queuing)
- **EC2/Elastic Beanstalk:** ~$10-20/month (t3.small instance)

**Total Monthly AWS Costs:** Approximately $25-55/month

---

## 12. Security & Compliance

### 12.1 Data Protection
- All student data encrypted at rest and in transit
- GDPR-compliant data handling practices
- Regular security audits and vulnerability assessments
- Secure file upload with virus scanning capabilities

### 12.2 Access Control
- Role-based permissions enforced at database and API levels
- Students can only access their own academic data
- Teachers can only access data for their assigned subjects and grades
- Audit logs for all sensitive operations

### 12.3 Backup & Recovery
- Daily automated database backups via RDS
- S3 file storage with versioning enabled
- Disaster recovery procedures documented
- Regular backup restoration testing

---

*This comprehensive PRD serves as the complete foundation for developing the Shiksha Learning Management System. The AWS-integrated architecture with Spring Modulith backend ensures scalability, security, and maintainability while providing a modern, efficient workflow for both teachers and students in the tuition center environment.*
