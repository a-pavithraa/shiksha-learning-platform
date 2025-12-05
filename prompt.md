I need to create the following post endpoint:
- An endpoint to upload completed assignment by student
- File should be in pdf format
- It should be uploaded in S3 with prefix /assignment-submissions/{assignmentId}/{studentId}
- The record must also be inserted in assignment-submissions table
- A notification must be sent to the teacher who uploaded the assignment
- If the assignment is past the due date, the notification must be sent to student also. In the notification sent to the teacher also, it must be mentioned