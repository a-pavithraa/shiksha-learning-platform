package com.shiksha.common;

import com.shiksha.common.models.ApiResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.util.stream.Collectors;

/**
 * Global exception handler for file upload related errors
 */
@RestControllerAdvice
public class FileUploadExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadExceptionHandler.class);

    /**
     * Handle S3 service exceptions
     */
    @ExceptionHandler(S3Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleS3Exception(S3Exception e) {
        logger.error("S3 operation failed: statusCode={}, error={}", e.statusCode(), e.getMessage(), e);
        
        if (e.statusCode() == 403) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Storage service access denied", "STORAGE_ACCESS_DENIED"));
        } else if (e.statusCode() == 404) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Storage bucket not found", "STORAGE_BUCKET_NOT_FOUND"));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to store assignment file", "STORAGE_ERROR"));
        }
    }

    /**
     * Handle custom S3 file service exceptions
     */
    @ExceptionHandler(S3FileService.S3FileException.class)
    public ResponseEntity<ApiResponse<Void>> handleS3FileException(S3FileService.S3FileException e) {
        logger.error("S3 file operation failed: {}", e.getMessage(), e);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to store assignment file", "STORAGE_ERROR"));
    }

    /**
     * Handle file size exceeded exceptions
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleMaxSizeException(MaxUploadSizeExceededException e) {
        logger.warn("File upload size exceeded: maxSize={}", e.getMaxUploadSize());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("File size exceeds maximum allowed size", "FILE_TOO_LARGE"));
    }

    /**
     * Handle validation constraint violations
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException e) {
        logger.warn("Validation constraint violation: {}", e.getMessage());
        
        String message = e.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        
        String errorCode = determineErrorCode(message);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message, errorCode));
    }

    /**
     * Handle method argument validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        logger.warn("Method argument validation failed: {}", e.getMessage());

        String message = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        String errorCode = determineErrorCode(message);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message, errorCode));
    }

    /**
     * Handle handler method validation errors (Spring 6.1+)
     */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleHandlerMethodValidation(HandlerMethodValidationException e) {
        logger.warn("Handler method validation failed: {}", e.getMessage());

        String message = e.getAllErrors()
                .stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        String errorCode = determineErrorCode(message);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message, errorCode));
    }

    /**
     * Handle illegal argument exceptions (business logic validation)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException e) {
        logger.warn("Illegal argument: {}", e.getMessage());
        
        String errorCode = determineErrorCode(e.getMessage());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage(), errorCode));
    }

    /**
     * Handle security exceptions
     */
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ApiResponse<Void>> handleSecurityException(SecurityException e) {
        logger.warn("Security exception: {}", e.getMessage());
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Authentication required", "UNAUTHORIZED"));
    }

    /**
     * Handle generic runtime exceptions
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(RuntimeException e) {
        logger.error("Unexpected runtime exception during file upload: {}", e.getMessage(), e);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An unexpected error occurred during file upload", "INTERNAL_ERROR"));
    }

    // Private helper methods

    private String determineErrorCode(String message) {
        if (message.contains("PDF format") || message.contains("pdf")) {
            return "INVALID_FILE_TYPE";
        } else if (message.contains("5MB") || message.contains("size exceeds")) {
            return "FILE_TOO_LARGE";
        } else if (message.contains("empty") || message.contains("required")) {
            return "EMPTY_FILE";
        } else if (message.contains("extension")) {
            return "INVALID_FILE_EXTENSION";
        } else if (message.contains("Assignment not found")) {
            return "ASSIGNMENT_NOT_FOUND";
        } else if (message.contains("Student not found")) {
            return "STUDENT_NOT_FOUND";
        } else if (message.contains("Only students can submit")) {
            return "FORBIDDEN";
        } else if (message.contains("not enrolled")) {
            return "STUDENT_NOT_ENROLLED";
        } else if (message.contains("not active")) {
            return "ASSIGNMENT_INACTIVE";
        } else {
            return "VALIDATION_ERROR";
        }
    }
}