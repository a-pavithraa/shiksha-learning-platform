# Shiksha LMS Infrastructure Variables

variable "project_name" {
  description = "Name of the project"
  type        = string
  default     = "shiksha-lms"
}

variable "environment" {
  description = "Environment (dev, staging, prod)"
  type        = string
  default     = "dev"
}

variable "aws_region" {
  description = "AWS region"
  type        = string
  default     = "us-east-1"
}

# S3 Configuration
variable "s3_bucket_name" {
  description = "S3 bucket name for file storage"
  type        = string
  default     = null
}

variable "s3_versioning_enabled" {
  description = "Enable versioning for S3 bucket"
  type        = bool
  default     = true
}

# SES Configuration
variable "ses_from_email" {
  description = "From email address for SES (must be verified)"
  type        = string
}

variable "verified_email_addresses" {
  description = "List of verified email addresses for SES"
  type        = list(string)
  default     = []
}

# SQS Configuration
variable "sqs_visibility_timeout" {
  description = "SQS message visibility timeout in seconds"
  type        = number
  default     = 300
}

variable "sqs_max_receive_count" {
  description = "Maximum number of times a message can be received before moving to DLQ"
  type        = number
  default     = 3
}

# Common Tags
variable "tags" {
  description = "Common tags for all resources"
  type        = map(string)
  default     = {
    Project     = "shiksha-lms"
    ManagedBy   = "terraform"
    Environment = "dev"
  }
}