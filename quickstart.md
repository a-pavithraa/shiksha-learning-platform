# Quickstart: Assignment Submission Upload

## Prerequisites
- Shiksha LMS application running locally
- Valid JWT token for a student user
- Assignment created by a teacher
- PDF file ready for upload (max 5MB)

## Test Scenarios

### 1. Successful Assignment Submission (On-time)

**Setup**:
```bash
# Prepare test PDF file
echo "This is a test assignment submission" > test_assignment.txt
# Convert to PDF or use existing PDF file
```

**Request**:
```bash
curl -X POST \
  "http://localhost:8080/api/assignments/1/submissions" \
  -H "Authorization: Bearer YOUR_STUDENT_JWT_TOKEN" \
  -H "Content-Type: multipart/form-data" \
  -F "file=@path/to/your/assignment.pdf"
```

**Expected Response** (201 Created):
```json
{
  "success": true,
  "message": "Assignment submitted successfully",
  "data": {
    "submissionId": 123,
    "fileName": "assignment.pdf",
    "submissionTimestamp": "2025-09-25T14:30:00",
    "isLate": false,
    "isResubmission": false
  }
}
```

**Verification Steps**:
1. ✅ File uploaded to S3 at `/assignment-submissions/1/{studentId}/assignment.pdf`
2. ✅ Record created in `assignment_submissions` table
3. ✅ Teacher receives notification email
4. ✅ No late submission notification sent

### 2. Late Assignment Submission

**Setup**:
- Ensure assignment due date is in the past
- Use same JWT token and PDF file

**Request**:
```bash
curl -X POST \
  "http://localhost:8080/api/assignments/1/submissions" \
  -H "Authorization: Bearer YOUR_STUDENT_JWT_TOKEN" \
  -H "Content-Type: multipart/form-data" \
  -F "file=@path/to/your/late_assignment.pdf"
```

**Expected Response** (201 Created):
```json
{
  "success": true,
  "message": "Late assignment submitted successfully",
  "data": {
    "submissionId": 124,
    "fileName": "late_assignment.pdf",
    "submissionTimestamp": "2025-09-25T23:45:00",
    "isLate": true,
    "isResubmission": false
  }
}
```

**Verification Steps**:
1. ✅ File uploaded to S3
2. ✅ Record created with `is_late = true`
3. ✅ Teacher receives notification with "LATE SUBMISSION" indicator
4. ✅ Student receives late submission notification

### 3. Assignment Resubmission

**Setup**:
- Use same assignment ID from scenario 1
- Different PDF file

**Request**:
```bash
curl -X POST \
  "http://localhost:8080/api/assignments/1/submissions" \
  -H "Authorization: Bearer YOUR_STUDENT_JWT_TOKEN" \
  -H "Content-Type: multipart/form-data" \
  -F "file=@path/to/your/revised_assignment.pdf"
```

**Expected Response** (201 Created):
```json
{
  "success": true,
  "message": "Assignment resubmitted successfully",
  "data": {
    "submissionId": 125,
    "fileName": "revised_assignment.pdf",
    "submissionTimestamp": "2025-09-25T16:20:00",
    "isLate": false,
    "isResubmission": true
  }
}
```

**Verification Steps**:
1. ✅ Previous file replaced in S3
2. ✅ Database record updated (or previous marked inactive)
3. ✅ Teacher receives notification with "RESUBMISSION" indicator

### 4. Validation Error - Invalid File Type

**Request**:
```bash
curl -X POST \
  "http://localhost:8080/api/assignments/1/submissions" \
  -H "Authorization: Bearer YOUR_STUDENT_JWT_TOKEN" \
  -H "Content-Type: multipart/form-data" \
  -F "file=@path/to/document.docx"
```

**Expected Response** (400 Bad Request):
```json
{
  "success": false,
  "message": "File must be PDF format",
  "error": "INVALID_FILE_TYPE"
}
```

### 5. Validation Error - File Too Large

**Setup**:
```bash
# Create a file larger than 5MB
dd if=/dev/zero of=large_file.pdf bs=1M count=6
```

**Request**:
```bash
curl -X POST \
  "http://localhost:8080/api/assignments/1/submissions" \
  -H "Authorization: Bearer YOUR_STUDENT_JWT_TOKEN" \
  -H "Content-Type: multipart/form-data" \
  -F "file=@large_file.pdf"
```

**Expected Response** (400 Bad Request):
```json
{
  "success": false,
  "message": "File size exceeds 5MB limit",
  "error": "FILE_TOO_LARGE"
}
```

### 6. Authentication Error

**Request**:
```bash
curl -X POST \
  "http://localhost:8080/api/assignments/1/submissions" \
  -H "Content-Type: multipart/form-data" \
  -F "file=@path/to/assignment.pdf"
```

**Expected Response** (401 Unauthorized):
```json
{
  "success": false,
  "message": "Authentication required",
  "error": "UNAUTHORIZED"
}
```

### 7. Authorization Error - Teacher Attempting Submission

**Request**:
```bash
curl -X POST \
  "http://localhost:8080/api/assignments/1/submissions" \
  -H "Authorization: Bearer YOUR_TEACHER_JWT_TOKEN" \
  -H "Content-Type: multipart/form-data" \
  -F "file=@path/to/assignment.pdf"
```

**Expected Response** (403 Forbidden):
```json
{
  "success": false,
  "message": "Only students can submit assignments",
  "error": "FORBIDDEN"
}
```

## Performance Tests

### Load Test - Multiple Concurrent Submissions
```bash
# Use Apache Bench or similar tool
ab -n 50 -c 5 -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -T "multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW" \
  -p post_data.txt \
  http://localhost:8080/api/assignments/1/submissions
```

**Expected**:
- Response time < 200ms per request
- All requests succeed (201 status)
- No S3 or database errors

### S3 Failure Simulation
1. Stop S3 LocalStack or disconnect from AWS
2. Attempt file upload
3. Verify 3 retry attempts
4. Verify 500 error response after retries

**Expected Response** (500 Internal Server Error):
```json
{
  "success": false,
  "message": "Failed to store assignment file",
  "error": "STORAGE_ERROR"
}
```

## Database Verification

After successful submissions, verify database state:

```sql
-- Check assignment submission records
SELECT * FROM assignment_submissions 
WHERE assignment_id = 1 
ORDER BY submission_timestamp DESC;

-- Check event publication (if using event store)
SELECT * FROM event_publication 
WHERE event_type = 'AssignmentSubmissionCreatedEvent' 
ORDER BY completion_date DESC;
```

## Email Notification Verification

1. Check teacher email for submission notifications
2. Check student email for late submission notifications
3. Verify notification content includes:
   - Assignment title
   - Student name
   - Submission timestamp
   - Late/resubmission indicators where applicable

## Cleanup

```sql
-- Remove test submissions
DELETE FROM assignment_submissions WHERE assignment_id = 1;
```

```bash
# Remove test files from S3
aws s3 rm s3://your-bucket/assignment-submissions/1/ --recursive
```