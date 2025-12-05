# Feature Specification: Assignment Submission Upload

**Feature Branch**: `main` (no separate branch)  
**Created**: 2025-09-25  
**Status**: Draft  
**Input**: User description: "I need to create the following post endpoint: - An endpoint to upload completed assignment by student - File should be in pdf format - It should be uploaded in S3 with prefix /assignment-submissions/{assignmentId}/{studentId} - The record must also be inserted in assignment-submissions table - A notification must be sent to the teacher who uploaded the assignment - If the assignment is past the due date, the notification must be sent to student also. In the notification sent to the teacher also, it must be mentioned"

---

## Clarifications

### Session 2025-09-25
- Q: What should be the maximum file size limit for PDF submissions? → A: 5MB maximum (typical for text-heavy PDFs)
- Q: How should the system handle when a student tries to submit multiple times for the same assignment? → A: Replace previous submission (overwrite existing file and record)
- Q: When should notifications be sent for duplicate submissions (when a student replaces their previous submission)? → A: Send notification with resubmission indicator
- Q: What should happen when S3 storage is unavailable during file upload? → A: Retry upload 3 times, then fail with error message
- Q: Should students be allowed to submit assignments after the due date has passed? → A: Always accept late submissions (with late notification)

## User Scenarios & Testing

### Primary User Story
A student completes an assignment and needs to submit it to their teacher. The student uploads their completed work as a PDF file through the system. The teacher is automatically notified when the submission is received, and if the submission is late, both the teacher and student are alerted about the late submission.

### Acceptance Scenarios
1. **Given** a student has a pending assignment, **When** they upload a valid PDF file before the due date, **Then** the file is stored, a database record is created, and the teacher receives a notification of the submission
2. **Given** a student submits an assignment after the due date, **When** they upload the PDF file, **Then** the submission is accepted but both teacher and student receive notifications indicating the late submission
3. **Given** a student attempts to upload a non-PDF file, **When** they try to submit, **Then** the system rejects the upload and displays an error message
4. **Given** a student tries to submit to a non-existent assignment, **When** they attempt upload, **Then** the system returns an error

### Edge Cases
- What happens when the uploaded file exceeds maximum size limits?
- How does the system handle duplicate submissions from the same student?
- What occurs when the S3 storage is unavailable during upload?
- How are notifications handled if the teacher's email is invalid?

## Requirements

### Functional Requirements
- **FR-001**: System MUST provide a POST endpoint for students to submit assignment files
- **FR-002**: System MUST validate that uploaded files are in PDF format only and do not exceed 5MB in size
- **FR-003**: System MUST store uploaded files in S3 with the path structure /assignment-submissions/{assignmentId}/{studentId}
- **FR-004**: System MUST create a record in the assignment-submissions table for each successful upload
- **FR-005**: System MUST send notification to the teacher when a student submits an assignment, including resubmission indicator if this replaces a previous submission
- **FR-006**: System MUST detect if submission is after the due date
- **FR-007**: System MUST notify both teacher and student when assignment is submitted late, with late status clearly indicated in notifications
- **FR-008**: System MUST validate that the student is enrolled in the subject/class for the assignment
- **FR-009**: System MUST validate that the assignment exists and is accepting submissions
- **FR-013**: System MUST accept submissions at any time after assignment creation, regardless of due date (late submissions trigger appropriate notifications)
- **FR-010**: System MUST return appropriate HTTP status codes and error messages for various scenarios
- **FR-011**: System MUST replace any previous submission when a student submits multiple times for the same assignment, overwriting both the file in S3 and the database record
- **FR-012**: System MUST retry S3 upload operations up to 3 times before failing and returning an error message to the student

### Key Entities
- **Assignment Submission**: Represents a student's submitted work, containing file location, submission timestamp, student ID, assignment ID, and submission status
- **Assignment**: The original assignment created by teacher, containing due date, subject information, and teacher ID
- **Notification**: Communication sent to users about assignment submission events, indicating recipient, message content, and submission status (on-time/late)

---

## Review & Acceptance Checklist

### Content Quality
- [x] No implementation details (languages, frameworks, APIs)
- [x] Focused on user value and business needs
- [x] Written for non-technical stakeholders
- [x] All mandatory sections completed

### Requirement Completeness
- [x] No [NEEDS CLARIFICATION] markers remain
- [x] Requirements are testable and unambiguous  
- [x] Success criteria are measurable
- [x] Scope is clearly bounded
- [x] Dependencies and assumptions identified

---

## Execution Status

- [x] User description parsed
- [x] Key concepts extracted
- [x] Ambiguities marked
- [x] User scenarios defined
- [x] Requirements generated
- [x] Entities identified
- [x] Review checklist passed

---