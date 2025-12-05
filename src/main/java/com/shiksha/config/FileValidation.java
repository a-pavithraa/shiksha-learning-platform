package com.shiksha.config;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import org.springframework.web.multipart.MultipartFile;

import java.lang.annotation.*;

/**
 * Validation annotations for file uploads
 */
public class FileValidation {

    /**
     * Validates that uploaded file is a PDF
     */
    @Documented
    @Constraint(validatedBy = ValidPdf.Validator.class)
    @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ValidPdf {
        String message() default "File must be PDF format";
        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};

        class Validator implements ConstraintValidator<ValidPdf, MultipartFile> {
            @Override
            public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
                if (file == null || file.isEmpty()) {
                    return false;
                }

                // Check MIME type
                String contentType = file.getContentType();
                if (!"application/pdf".equals(contentType)) {
                    return false;
                }

                // Check file extension
                String filename = file.getOriginalFilename();
                if (filename == null || !filename.toLowerCase().endsWith(".pdf")) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate("File extension must be .pdf")
                           .addConstraintViolation();
                    return false;
                }

                return true;
            }
        }
    }

    /**
     * Validates file size is within specified limit
     */
    @Documented
    @Constraint(validatedBy = ValidFileSize.Validator.class)
    @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ValidFileSize {
        String message() default "File size exceeds limit";
        long maxSizeBytes() default 5_242_880L; // 5MB default
        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};

        class Validator implements ConstraintValidator<ValidFileSize, MultipartFile> {
            private long maxSizeBytes;

            @Override
            public void initialize(ValidFileSize constraintAnnotation) {
                this.maxSizeBytes = constraintAnnotation.maxSizeBytes();
            }

            @Override
            public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
                if (file == null) {
                    return true; // Let @NotNull handle null validation
                }

                if (file.isEmpty()) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate("File cannot be empty")
                           .addConstraintViolation();
                    return false;
                }

                if (file.getSize() > maxSizeBytes) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate(
                            String.format("File size exceeds %dMB limit", maxSizeBytes / (1024 * 1024)))
                           .addConstraintViolation();
                    return false;
                }

                return true;
            }
        }
    }

    /**
     * Validates that file is not empty
     */
    @Documented
    @Constraint(validatedBy = NotEmptyFile.Validator.class)
    @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface NotEmptyFile {
        String message() default "File is required";
        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};

        class Validator implements ConstraintValidator<NotEmptyFile, MultipartFile> {
            @Override
            public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
                if (file == null) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate("File is required")
                           .addConstraintViolation();
                    return false;
                }

                if (file.isEmpty()) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate("File cannot be empty")
                           .addConstraintViolation();
                    return false;
                }

                return true;
            }
        }
    }

    /**
     * Combined annotation for assignment submission files
     */
    @Documented
    @Target({ElementType.FIELD, ElementType.PARAMETER})
    @Retention(RetentionPolicy.RUNTIME)
    @NotEmptyFile
    @ValidPdf
    @ValidFileSize(maxSizeBytes = 5_242_880L)
    @Constraint(validatedBy = {})
    public @interface ValidAssignmentFile {
        String message() default "Invalid assignment file";
        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};
    }
}