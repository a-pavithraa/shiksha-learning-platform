# Shiksha LMS Infrastructure

This directory contains Terraform configuration for the AWS infrastructure supporting the Shiksha Learning Management System.

## 🏗️ Infrastructure Components

### Created Resources

- **S3 Bucket**: Secure file storage for assignments and submissions
- **SQS Queues**: Message queuing for email notifications
- **SES Configuration**: Email service for automated notifications
- **IAM Roles & Policies**: Secure access control for application

### Key Features

- **Security**: Private S3 bucket with encryption, IAM roles with least privilege
- **Reliability**: SQS dead letter queue for failed notifications
- **Scalability**: Lifecycle policies for cost optimization
- **Monitoring**: SES event tracking via SNS

## 🚀 Deployment Instructions

### Prerequisites

1. AWS CLI configured with appropriate credentials
2. Terraform >= 1.0 installed
3. Access to an AWS account with permission to create resources

### Setup Steps

1. **Copy configuration file**:
   ```bash
   cp terraform.tfvars.example terraform.tfvars
   ```

2. **Update terraform.tfvars** with your values:
   ```bash
   # Required: Update with your verified email
   ses_from_email = "noreply@your-domain.com"
   
   # Add test email addresses
   verified_email_addresses = [
     "teacher@your-domain.com",
     "student@your-domain.com"
   ]
   ```

3. **Initialize Terraform**:
   ```bash
   terraform init
   ```

4. **Plan deployment**:
   ```bash
   terraform plan
   ```

5. **Apply configuration**:
   ```bash
   terraform apply
   ```

### Post-Deployment

1. **Verify SES emails**: Check AWS SES console and verify email addresses
2. **Note outputs**: Save the terraform outputs for Spring Boot configuration
3. **Test SQS**: Verify queues are created and accessible

## 📋 Configuration for Spring Boot

After deployment, use these Terraform outputs in your Spring Boot application:

```yaml
# application.yml
cloud:
  aws:
    region: ${AWS_REGION}
    s3:
      bucket: ${S3_BUCKET_NAME}
    sqs:
      notification-queue-url: ${SQS_NOTIFICATION_QUEUE_URL}
    ses:
      configuration-set: ${SES_CONFIGURATION_SET}
      from-email: ${SES_FROM_EMAIL}
```

## 🏷️ Resource Naming

All resources follow this naming pattern:
- Format: `{project_name}-{environment}-{resource_type}-{random_suffix}`
- Example: `shiksha-lms-dev-files-abc123xy`

## 💰 Cost Estimation

Expected monthly costs (development environment):
- **S3**: ~$1-3 (file storage)
- **SQS**: ~$0.50 (message processing)
- **SES**: ~$0.10 (email sending)
- **Total**: ~$2-5/month

## 🔒 Security Features

- **S3 Bucket**: Private access only, server-side encryption
- **IAM Roles**: Least privilege access policies
- **SQS**: Secure message queuing with DLQ
- **SES**: TLS-required email delivery

## 🧹 Cleanup

To destroy all resources:
```bash
terraform destroy
```

**⚠️ Warning**: This will permanently delete all files in S3 and cannot be undone.

## 📖 Additional Resources

- [Terraform AWS Provider Documentation](https://registry.terraform.io/providers/hashicorp/aws/latest/docs)
- [AWS S3 Best Practices](https://docs.aws.amazon.com/AmazonS3/latest/userguide/security-best-practices.html)
- [AWS SES Developer Guide](https://docs.aws.amazon.com/ses/latest/dg/)
- [AWS SQS Developer Guide](https://docs.aws.amazon.com/AWSSimpleQueueService/latest/SQSDeveloperGuide/)