# Shiksha LMS Infrastructure Outputs

# =====================================================
# S3 OUTPUTS
# =====================================================

output "s3_bucket_name" {
  description = "Name of the S3 bucket"
  value       = aws_s3_bucket.shiksha_files.bucket
}

output "s3_bucket_arn" {
  description = "ARN of the S3 bucket"
  value       = aws_s3_bucket.shiksha_files.arn
}

output "s3_bucket_domain_name" {
  description = "Domain name of the S3 bucket"
  value       = aws_s3_bucket.shiksha_files.bucket_domain_name
}

output "s3_bucket_regional_domain_name" {
  description = "Regional domain name of the S3 bucket"
  value       = aws_s3_bucket.shiksha_files.bucket_regional_domain_name
}

# =====================================================
# SQS OUTPUTS
# =====================================================

output "notification_queue_name" {
  description = "Name of the notification queue"
  value       = aws_sqs_queue.notification_queue.name
}

output "notification_queue_url" {
  description = "URL of the notification queue"
  value       = aws_sqs_queue.notification_queue.url
}

output "notification_queue_arn" {
  description = "ARN of the notification queue"
  value       = aws_sqs_queue.notification_queue.arn
}

output "notification_dlq_name" {
  description = "Name of the notification dead letter queue"
  value       = aws_sqs_queue.notification_dlq.name
}

output "notification_dlq_url" {
  description = "URL of the notification dead letter queue"
  value       = aws_sqs_queue.notification_dlq.url
}

output "notification_dlq_arn" {
  description = "ARN of the notification dead letter queue"
  value       = aws_sqs_queue.notification_dlq.arn
}

# =====================================================
# SES OUTPUTS
# =====================================================

output "ses_configuration_set_name" {
  description = "SES configuration set name"
  value       = aws_ses_configuration_set.shiksha_config_set.name
}

output "ses_from_email" {
  description = "SES from email address"
  value       = var.ses_from_email
}

output "verified_email_addresses" {
  description = "List of verified email addresses"
  value       = var.verified_email_addresses
}

# =====================================================
# IAM OUTPUTS
# =====================================================

output "app_role_name" {
  description = "Name of the application IAM role"
  value       = aws_iam_role.app_role.name
}

output "app_role_arn" {
  description = "ARN of the application IAM role"
  value       = aws_iam_role.app_role.arn
}

output "app_instance_profile_name" {
  description = "Name of the application instance profile"
  value       = aws_iam_instance_profile.app_profile.name
}

output "app_instance_profile_arn" {
  description = "ARN of the application instance profile"
  value       = aws_iam_instance_profile.app_profile.arn
}

# =====================================================
# CONFIGURATION FOR APPLICATION
# =====================================================

output "spring_config" {
  description = "Configuration values for Spring Boot application"
  value = {
    aws = {
      region     = var.aws_region
      s3 = {
        bucket = aws_s3_bucket.shiksha_files.bucket
      }
      sqs = {
        notification_queue_url = aws_sqs_queue.notification_queue.url
        dlq_url               = aws_sqs_queue.notification_dlq.url
      }
      ses = {
        configuration_set = aws_ses_configuration_set.shiksha_config_set.name
        from_email       = var.ses_from_email
      }
    }
  }
  sensitive = false
}

# =====================================================
# RESOURCE SUMMARY
# =====================================================

output "resource_summary" {
  description = "Summary of created resources"
  value = {
    environment     = var.environment
    project_name    = var.project_name
    aws_region      = var.aws_region
    s3_bucket       = aws_s3_bucket.shiksha_files.bucket
    sqs_queue       = aws_sqs_queue.notification_queue.name
    ses_config_set  = aws_ses_configuration_set.shiksha_config_set.name
    iam_role        = aws_iam_role.app_role.name
  }
}