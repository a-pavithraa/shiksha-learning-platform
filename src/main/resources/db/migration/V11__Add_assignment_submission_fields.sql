-- Add missing fields to assignment_submissions table for file size tracking and submission metadata

-- Add new columns to track file size and submission characteristics
ALTER TABLE assignment_submissions 
ADD COLUMN file_size BIGINT,
ADD COLUMN is_late BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN is_resubmission BOOLEAN NOT NULL DEFAULT FALSE;

-- Add check constraint for file size (5MB = 5,242,880 bytes)
ALTER TABLE assignment_submissions 
ADD CONSTRAINT check_file_size 
CHECK (file_size IS NULL OR (file_size > 0 AND file_size <= 5242880));

-- Add comment to document the constraint
COMMENT ON CONSTRAINT check_file_size ON assignment_submissions IS 'Ensures file size is positive and within 5MB limit (5,242,880 bytes)';