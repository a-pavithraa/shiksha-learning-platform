# Data Model: Assignment Submission Upload

## Core Entities

### AssignmentSubmission (Existing Entity)
**Purpose**: Represents a student's submitted assignment file and metadata

**Database Attributes** (from existing schema):
- `id` (BIGSERIAL): Primary key, auto-generated
- `assignment_id` (BIGINT): Foreign key to assignments table
- `student_id` (BIGINT): Foreign key to users table  
- `file_path` (VARCHAR(500)): S3 object key/path
- `file_name` (VARCHAR(255)): Original filename of uploaded file
- `submitted_at` (TIMESTAMP): When submission was uploaded, defaults to CURRENT_TIMESTAMP
- `status` (submission_status_enum): Status of submission, defaults to 'SUBMITTED'
- `teacher_feedback` (TEXT): Optional feedback from teacher (nullable)

**Additional Attributes Needed** (will be added via new migration):
- `file_size` (BIGINT): File size in bytes (max 5MB = 5,242,880 bytes)
- `is_late` (BOOLEAN): Whether submission was after due date
- `is_resubmission` (BOOLEAN): Whether this replaces a previous submission

**Validation Rules**:
- `assignment_id`: Must exist in assignments table
- `student_id`: Must exist in users table with STUDENT role
- `file_name`: Non-null, non-empty, must end with .pdf
- `file_path`: Non-null, follows S3 path pattern
- `file_size`: Between 1 and 5,242,880 bytes (when added)
- `submitted_at`: Non-null, cannot be future date

**Relationships**:
- Belongs to Assignment (Many-to-One) via assignment_id
- Belongs to User/Student (Many-to-One) via student_id
- Unique constraint on (assignment_id, student_id) - one active submission per student per assignment

**State Transitions**:
- SUBMITTED (initial state)
- REVIEWED (teacher has reviewed)  
- GRADED (teacher has provided grade)

**Enum Values**:
- submission_status_enum: 'SUBMITTED', 'REVIEWED', 'GRADED'

## Domain Events

### AssignmentSubmissionCreatedEvent
**Purpose**: Published when a student submits an assignment

**Attributes**:
- `submissionId` (Long): ID of created submission
- `assignmentId` (Long): Assignment being submitted to
- `studentId` (Long): Student who submitted
- `teacherId` (Long): Teacher who created the assignment
- `isLate` (Boolean): Whether submission was late
- `isResubmission` (Boolean): Whether this replaced a previous submission
- `submissionTimestamp` (LocalDateTime): When event occurred

**Event Handlers**:
- `NotificationModule.AssignmentSubmissionEventListener`: Sends notifications to teacher and student

## Integration Points

### Existing Entities (Referenced)

#### Assignment (Existing Entity)
- `id` (BIGINT): Referenced by assignment_submissions.assignment_id
- `teacher_id` (BIGINT): Used for notification routing, references users(id)
- `subject_id` (BIGINT): References subjects table
- `grade_level` (INTEGER): Grade level constraint (9-12)
- `title` (VARCHAR(255)): Used in notification messages
- `description` (TEXT): Assignment description (nullable)
- `file_path` (VARCHAR(500)): S3 object key for assignment file
- `file_name` (VARCHAR(255)): Original assignment filename
- `due_date` (DATE): Used for late submission detection (nullable)
- `created_at`, `updated_at` (TIMESTAMP): Audit fields
- `is_active` (BOOLEAN): Soft delete flag, defaults to TRUE

#### User (Student/Teacher)
- `id` (Long): Referenced by AssignmentSubmission.studentId
- `email` (String): Used for notification delivery
- `firstName`, `lastName` (String): Used in notification personalization
- `role` (UserRole): Must be STUDENT for submissions

### External Dependencies

#### AWS S3
- **Bucket**: Configured via S3Config
- **Path Pattern**: `/assignment-submissions/{assignmentId}/{studentId}/{fileName}`
- **Access**: Upload/read permissions for application service account

#### Database Schema Changes
**Table `assignment_submissions` exists** - requires migration to add missing columns:

**Current Schema**:
```sql
-- From V3__Create_academic_tables.sql
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
```

**Required Migration** (new Flyway script):
```sql
-- V11__Add_assignment_submission_fields.sql
ALTER TABLE assignment_submissions 
ADD COLUMN file_size BIGINT,
ADD COLUMN is_late BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN is_resubmission BOOLEAN NOT NULL DEFAULT FALSE;

-- Add check constraint for file size (5MB = 5,242,880 bytes)
ALTER TABLE assignment_submissions 
ADD CONSTRAINT check_file_size 
CHECK (file_size IS NULL OR (file_size > 0 AND file_size <= 5242880));
```

## Data Flow

### Submission Process
1. Student uploads file via REST endpoint
2. Controller validates multipart file (PDF, size)
3. Service validates business rules (student enrollment, assignment exists)
4. Service uploads file to S3 with retry logic
5. Service saves AssignmentSubmission entity
6. Service publishes AssignmentSubmissionCreatedEvent
7. NotificationModule handles event, sends notifications

### Error Scenarios
- **File validation failure**: Return 400 Bad Request
- **S3 upload failure**: Retry 3 times, then return 500 Internal Server Error
- **Database constraint violation**: Return 400 Bad Request
- **Authentication failure**: Return 401 Unauthorized
- **Authorization failure**: Return 403 Forbidden

## Performance Considerations

### Database Indexes
- Index on (assignment_id, student_id) for unique constraint
- Index on assignment_id for teacher queries
- Index on student_id for student dashboard queries
- Index on submission_timestamp for chronological queries

### File Storage
- S3 storage provides high availability and scalability
- File URLs generated deterministically for caching
- Multipart upload for large files (though 5MB limit reduces need)