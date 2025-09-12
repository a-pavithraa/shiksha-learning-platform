# Shiksha LMS Infrastructure Configuration

terraform {
  required_version = ">= 1.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
    random = {
      source  = "hashicorp/random"
      version = "~> 3.4"
    }
  }
}

provider "aws" {
  region = var.aws_region
}

# Generate random suffix for unique resource naming
resource "random_string" "suffix" {
  length  = 8
  special = false
  upper   = false
}

locals {
  resource_prefix = "${var.project_name}-${var.environment}"
  bucket_name     = var.s3_bucket_name != null ? var.s3_bucket_name : "${local.resource_prefix}-files-${random_string.suffix.result}"
  
  common_tags = merge(var.tags, {
    Environment = var.environment
    Project     = var.project_name
  })
}

# =====================================================
# S3 BUCKET CONFIGURATION
# =====================================================

# Main S3 bucket for file storage
resource "aws_s3_bucket" "shiksha_files" {
  bucket = local.bucket_name
  tags   = local.common_tags
}

# S3 bucket versioning
resource "aws_s3_bucket_versioning" "shiksha_files_versioning" {
  bucket = aws_s3_bucket.shiksha_files.id
  versioning_configuration {
    status = var.s3_versioning_enabled ? "Enabled" : "Suspended"
  }
}

# S3 bucket server-side encryption
resource "aws_s3_bucket_server_side_encryption_configuration" "shiksha_files_encryption" {
  bucket = aws_s3_bucket.shiksha_files.id

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
    bucket_key_enabled = true
  }
}

# S3 bucket public access block
resource "aws_s3_bucket_public_access_block" "shiksha_files_pab" {
  bucket = aws_s3_bucket.shiksha_files.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

# S3 bucket lifecycle configuration
resource "aws_s3_bucket_lifecycle_configuration" "shiksha_files_lifecycle" {
  bucket = aws_s3_bucket.shiksha_files.id

  rule {
    id     = "cleanup_old_versions"
    status = "Enabled"

    noncurrent_version_expiration {
      noncurrent_days = 30
    }

    abort_incomplete_multipart_upload {
      days_after_initiation = 7
    }
  }

  rule {
    id     = "transition_to_ia"
    status = "Enabled"

    transition {
      days          = 30
      storage_class = "STANDARD_IA"
    }

    transition {
      days          = 90
      storage_class = "GLACIER"
    }
  }
}

# S3 bucket CORS configuration
resource "aws_s3_bucket_cors_configuration" "shiksha_files_cors" {
  bucket = aws_s3_bucket.shiksha_files.id

  cors_rule {
    allowed_headers = ["*"]
    allowed_methods = ["GET", "PUT", "POST", "DELETE", "HEAD"]
    allowed_origins = ["*"] # In production, restrict to your domain
    expose_headers  = ["ETag"]
    max_age_seconds = 3000
  }
}

# =====================================================
# SQS CONFIGURATION
# =====================================================

# Dead Letter Queue for failed notifications
resource "aws_sqs_queue" "notification_dlq" {
  name                      = "${local.resource_prefix}-notification-dlq"
  message_retention_seconds = 1209600 # 14 days
  
  tags = local.common_tags
}

# Main notification queue
resource "aws_sqs_queue" "notification_queue" {
  name                      = "${local.resource_prefix}-notifications"
  visibility_timeout_seconds = var.sqs_visibility_timeout
  message_retention_seconds = 1209600 # 14 days
  max_message_size          = 262144  # 256 KB
  delay_seconds             = 0
  receive_wait_time_seconds = 20      # Long polling

  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.notification_dlq.arn
    maxReceiveCount     = var.sqs_max_receive_count
  })

  tags = local.common_tags
}

# =====================================================
# SES CONFIGURATION
# =====================================================

# SES Email Identities for verified addresses
resource "aws_ses_email_identity" "verified_emails" {
  count = length(var.verified_email_addresses)
  email = var.verified_email_addresses[count.index]
}

# SES Configuration Set
resource "aws_ses_configuration_set" "shiksha_config_set" {
  name = "${local.resource_prefix}-config-set"

  delivery_options {
    tls_policy = "Require"
  }
}

# SES Event Destination for bounces and complaints
resource "aws_ses_event_destination" "notification_events" {
  name                   = "${local.resource_prefix}-events"
  configuration_set_name = aws_ses_configuration_set.shiksha_config_set.name
  enabled                = true
  matching_types         = ["bounce", "complaint", "reject"]

  sns_destination {
    topic_arn = aws_sns_topic.ses_events.arn
  }
}

# SNS Topic for SES events
resource "aws_sns_topic" "ses_events" {
  name = "${local.resource_prefix}-ses-events"
  tags = local.common_tags
}

# =====================================================
# IAM ROLES AND POLICIES
# =====================================================

# IAM Role for Application EC2 instances
resource "aws_iam_role" "app_role" {
  name = "${local.resource_prefix}-app-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "ec2.amazonaws.com"
        }
      }
    ]
  })

  tags = local.common_tags
}

# IAM Policy for S3 Access
resource "aws_iam_policy" "s3_access_policy" {
  name        = "${local.resource_prefix}-s3-access"
  description = "Policy for S3 access"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "s3:GetObject",
          "s3:PutObject",
          "s3:DeleteObject",
          "s3:GetObjectVersion",
          "s3:ListBucket"
        ]
        Resource = [
          aws_s3_bucket.shiksha_files.arn,
          "${aws_s3_bucket.shiksha_files.arn}/*"
        ]
      }
    ]
  })
}

# IAM Policy for SQS Access
resource "aws_iam_policy" "sqs_access_policy" {
  name        = "${local.resource_prefix}-sqs-access"
  description = "Policy for SQS access"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "sqs:SendMessage",
          "sqs:ReceiveMessage",
          "sqs:DeleteMessage",
          "sqs:GetQueueAttributes",
          "sqs:GetQueueUrl"
        ]
        Resource = [
          aws_sqs_queue.notification_queue.arn,
          aws_sqs_queue.notification_dlq.arn
        ]
      }
    ]
  })
}

# IAM Policy for SES Access
resource "aws_iam_policy" "ses_access_policy" {
  name        = "${local.resource_prefix}-ses-access"
  description = "Policy for SES access"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "ses:SendEmail",
          "ses:SendRawEmail",
          "ses:GetSendQuota",
          "ses:GetSendStatistics"
        ]
        Resource = "*"
      }
    ]
  })
}

# Attach policies to the application role
resource "aws_iam_role_policy_attachment" "app_s3_access" {
  role       = aws_iam_role.app_role.name
  policy_arn = aws_iam_policy.s3_access_policy.arn
}

resource "aws_iam_role_policy_attachment" "app_sqs_access" {
  role       = aws_iam_role.app_role.name
  policy_arn = aws_iam_policy.sqs_access_policy.arn
}

resource "aws_iam_role_policy_attachment" "app_ses_access" {
  role       = aws_iam_role.app_role.name
  policy_arn = aws_iam_policy.ses_access_policy.arn
}

# Instance profile for EC2
resource "aws_iam_instance_profile" "app_profile" {
  name = "${local.resource_prefix}-app-profile"
  role = aws_iam_role.app_role.name
}