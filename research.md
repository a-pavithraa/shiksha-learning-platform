# Research: Assignment Submission Upload

## Technical Stack Decisions

### File Upload Handling
- **Decision**: Spring Boot MultipartFile with AWS S3 SDK
- **Rationale**: Already established in project architecture, integrates well with existing S3Config
- **Alternatives considered**: Direct HTTP uploads to S3 (rejected - requires complex presigned URL management)

### Database Schema
- **Decision**: Extend existing assignment_submissions table structure
- **Rationale**: Table already exists in schema, aligns with current entity relationships
- **Alternatives considered**: New table (rejected - would duplicate existing structure)

### Event-Driven Architecture
- **Decision**: Spring Modulith domain events for cross-module communication
- **Rationale**: Already established pattern in project for AssignmentCreatedEvent
- **Alternatives considered**: Direct service calls (rejected - violates modular architecture)

### File Storage Strategy
- **Decision**: S3 with prefix structure /assignment-submissions/{assignmentId}/{studentId}
- **Rationale**: Provides clear organization, enables efficient querying, supports multi-tenancy
- **Alternatives considered**: Flat file structure (rejected - poor organization), database BLOB storage (rejected - performance concerns)

### Retry Mechanism
- **Decision**: Spring Retry with exponential backoff for S3 operations
- **Rationale**: Handles transient failures gracefully, configurable retry policies
- **Alternatives considered**: Manual retry logic (rejected - more error-prone), Circuit breaker pattern (rejected - overkill for this use case)

## Integration Patterns

### Multipart File Validation
- **Decision**: Custom validator annotation with PDF MIME type and size checks
- **Rationale**: Reusable validation logic, clear error messages, Spring Boot integration
- **Alternatives considered**: Manual validation in controller (rejected - not reusable)

### Late Submission Detection
- **Decision**: Compare submission timestamp with assignment due_date in service layer
- **Rationale**: Business logic belongs in service, enables easy testing
- **Alternatives considered**: Database triggers (rejected - harder to test), client-side detection (rejected - not secure)

## Security Considerations

### File Validation
- **Decision**: MIME type validation + file extension check + content validation
- **Rationale**: Multi-layer security approach prevents malicious file uploads
- **Alternatives considered**: Extension-only validation (rejected - easily bypassed)

### Access Control
- **Decision**: JWT-based authentication with student role validation
- **Rationale**: Consistent with existing authentication pattern
- **Alternatives considered**: Session-based auth (rejected - not stateless)

## Performance Optimizations

### File Upload
- **Decision**: Stream processing for large files, concurrent S3 uploads
- **Rationale**: Memory efficient, faster upload times
- **Alternatives considered**: Load entire file to memory (rejected - memory concerns)

### Database Operations
- **Decision**: Batch operations where possible, indexed queries on assignment_id and student_id
- **Rationale**: Efficient querying for common access patterns
- **Alternatives considered**: Individual operations (rejected - performance impact)

## Conclusion

All technical decisions align with existing Shiksha LMS architecture and Spring Modulith patterns. No new technology introduction required. Implementation can proceed with Phase 1 design.