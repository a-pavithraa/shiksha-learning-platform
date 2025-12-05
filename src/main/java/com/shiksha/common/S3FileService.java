package com.shiksha.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;

@Service
public class S3FileService {

    private static final Logger logger = LoggerFactory.getLogger(S3FileService.class);

    private final S3Client s3Client;
    private final String bucketName;
    private final String region;

    public S3FileService(S3Client s3Client,
                        @Value("${aws.s3.bucket-name}") String bucketName,
                        @Value("${aws.s3.region}") String region) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
        this.region = region;
    }

    /**
     * Upload file to S3 with retry logic for transient failures
     */
    @Retryable(
            retryFor = {S3Exception.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2.0)
    )
    public String uploadFile(String key, MultipartFile file) {
        try {
            logger.info("Uploading file to S3: bucket={}, key={}, size={}", bucketName, key, file.getSize());

            // Validate file
            validateFile(file);

            // Create put object request
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            // Upload file
            PutObjectResponse response = s3Client.putObject(putObjectRequest, 
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            // Generate file URL
            String fileUrl = generateFileUrl(key);
            
            logger.info("Successfully uploaded file to S3: key={}, etag={}", key, response.eTag());
            return fileUrl;

        } catch (IOException e) {
            logger.error("Failed to read file for S3 upload: key={}", key, e);
            throw new S3FileException("Failed to read file for upload", e);
        } catch (S3Exception e) {
            logger.error("S3 operation failed: key={}, error={}", key, e.getMessage(), e);
            
            // Don't retry authentication/authorization errors
            if (isNonRetryableError(e)) {
                throw new S3FileException("S3 authentication/authorization error", e);
            }
            
            throw e; // Let retry mechanism handle this
        } catch (Exception e) {
            logger.error("Unexpected error during S3 upload: key={}", key, e);
            throw new S3FileException("Unexpected error during file upload", e);
        }
    }

    /**
     * Delete file from S3 (used for resubmissions)
     */
    public void deleteFile(String key) {
        try {
            logger.info("Deleting file from S3: bucket={}, key={}", bucketName, key);

            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteRequest);
            
            logger.info("Successfully deleted file from S3: key={}", key);

        } catch (S3Exception e) {
            logger.error("Failed to delete file from S3: key={}, error={}", key, e.getMessage(), e);
            // Don't throw exception for delete failures - log and continue
        }
    }

    /**
     * Generate pre-signed URL for file download (optional feature)
     */
    public String generatePresignedUrl(String key, int expirationMinutes) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            // Note: Pre-signed URL generation would require additional SDK setup
            // For now, return public URL (if bucket is configured for public access)
            return generateFileUrl(key);

        } catch (Exception e) {
            logger.error("Failed to generate pre-signed URL: key={}", key, e);
            throw new S3FileException("Failed to generate download URL", e);
        }
    }

    /**
     * Check if file exists in S3
     */
    public boolean fileExists(String key) {
        try {
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.headObject(headRequest);
            return true;

        } catch (NoSuchKeyException e) {
            return false;
        } catch (S3Exception e) {
            logger.error("Error checking file existence: key={}, error={}", key, e.getMessage());
            return false;
        }
    }

    // Private helper methods

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be null or empty");
        }
        
        if (file.getSize() > 5_242_880L) { // 5MB limit
            throw new IllegalArgumentException("File size exceeds 5MB limit");
        }
    }

    private String generateFileUrl(String key) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, key);
    }

    private boolean isNonRetryableError(S3Exception e) {
        // Don't retry authentication, authorization, or client errors
        int statusCode = e.statusCode();
        return statusCode == 401 || statusCode == 403 || statusCode == 404 || 
               (statusCode >= 400 && statusCode < 500);
    }

    /**
     * Custom exception for S3 file operations
     */
    public static class S3FileException extends RuntimeException {
        public S3FileException(String message) {
            super(message);
        }

        public S3FileException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public String generatePresignedDownloadUrl(String fileKey) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(fileKey)
                .build();

        // For now, return a simple S3 URL. In production, you would use presigned URLs
        return "https://" + bucketName + ".s3.amazonaws.com/" + fileKey;
    }

}