# Implementation Plan: Assignment Submission Upload

**Branch**: `main` | **Date**: 2025-09-25 | **Spec**: [assignment-submission-upload-spec.md](assignment-submission-upload-spec.md)
**Input**: Feature specification from `assignment-submission-upload-spec.md`

## Summary
Student assignment submission upload endpoint for the Shiksha LMS. Enables students to upload PDF assignments to S3 storage with automated teacher notifications, late submission detection, and robust error handling with retry mechanisms.

## Technical Context
**Language/Version**: Java 21  
**Primary Dependencies**: Spring Boot 3.5.5, Spring Modulith, Spring Security, AWS SDK S3, PostgreSQL, Flyway  
**Storage**: PostgreSQL database + AWS S3 for file storage  
**Testing**: JUnit 5, Testcontainers for integration testing  
**Target Platform**: Web server (Spring Boot application)
**Project Type**: Single Spring Boot application with modular architecture  
**Performance Goals**: <200ms response time for upload operations, support for 5MB files  
**Constraints**: PDF format only, 5MB max file size, 3-retry policy for S3 failures  
**Scale/Scope**: Educational platform supporting multiple students and teachers per assignment

## Constitution Check
*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

**I. Modular Architecture (NON-NEGOTIABLE)**: ✅ PASS
- Feature will span academic module (assignment submission) and notification module (alerts)
- Cross-module communication via domain events (AssignmentSubmissionCreatedEvent)
- Clear module boundaries maintained

**II. Layer Separation & Data Transfer**: ✅ PASS  
- Controller will use SubmitAssignmentCommand object
- Service will return AssignmentSubmissionDto, not entities
- Clear separation maintained throughout

**III. Test-First Development (NON-NEGOTIABLE)**: ✅ PASS
- Contract tests will be written first
- Integration tests using Testcontainers for S3 and database operations
- TDD approach enforced

**IV. Event-Driven Communication**: ✅ PASS
- AssignmentSubmissionCreatedEvent for notification triggers
- Proper event handling between academic and notification modules

**V. Spring Boot Standards & Best Practices (NON-NEGOTIABLE)**: ✅ PASS
- Constructor injection with final fields
- @ConfigurationProperties for S3 settings
- REST endpoint with proper HTTP status codes
- Centralized exception handling

**VI. Security & Data Protection**: ✅ PASS
- JWT authentication required
- File validation (PDF only, 5MB limit)
- S3 storage with proper access controls

## Project Structure

### Documentation (this feature)
```
assignment-submission-upload-plan.md     # This file
research.md                              # Phase 0 output
data-model.md                           # Phase 1 output  
quickstart.md                           # Phase 1 output
contracts/                              # Phase 1 output
└── assignment-submission-api.yaml
```

### Source Code (repository root - Spring Modulith structure)
```
src/main/java/com/shiksha/
├── academic/                           # Assignment submission handling
│   ├── domain/
│   │   ├── AssignmentSubmission.java
│   │   ├── AssignmentSubmissionRepository.java
│   │   └── AssignmentSubmissionService.java
│   ├── web/
│   │   ├── controller/AssignmentSubmissionController.java
│   │   └── dto/SubmitAssignmentRequest.java
│   └── domain/command/SubmitAssignmentCommand.java
├── notification/                       # Notification handling
│   └── domain/AssignmentSubmissionEventListener.java
└── shared/                            # S3 service integration
    └── s3/S3FileService.java

src/test/java/com/shiksha/
├── academic/                          # Tests for academic module
└── integration/                       # Integration tests
```

**Structure Decision**: Spring Modulith single application structure

## Phase 0: Outline & Research

No unknowns requiring research - all technical context is clear from existing project setup:
- Spring Boot 3.5.5 with Spring Modulith already configured
- AWS S3 integration already present in project
- PostgreSQL database with Flyway migrations established
- Testcontainers testing infrastructure in place

**Output**: research.md (minimal - leveraging existing architecture decisions)

## Phase 1: Design & Contracts

1. **Extract entities from feature spec** → `data-model.md`:
   - AssignmentSubmission (student_id, assignment_id, file_url, submission_timestamp, is_late)
   - Domain events: AssignmentSubmissionCreatedEvent
   - Integration with existing User, Assignment entities

2. **Generate API contracts** from functional requirements:
   - POST /api/assignments/{assignmentId}/submissions endpoint
   - Multipart file upload with metadata
   - OpenAPI specification with validation rules

3. **Generate contract tests** from contracts:
   - Upload success scenarios (on-time, late)
   - Validation failure scenarios (non-PDF, oversized)
   - Authentication and authorization tests

4. **Extract test scenarios** from user stories:
   - Student uploads assignment before due date
   - Student uploads assignment after due date
   - Student replaces previous submission
   - S3 failures and retry scenarios

5. **Update agent file incrementally**:
   - Update CLAUDE.md with assignment submission context

**Output**: data-model.md, /contracts/*, failing tests, quickstart.md, CLAUDE.md updates

## Phase 2: Task Planning Approach
*This section describes what the /tasks command will do - DO NOT execute during /plan*

**Task Generation Strategy**:
- Contract test creation (parallel tasks for each endpoint)
- Entity and repository creation (AssignmentSubmission)
- Service layer implementation (AssignmentSubmissionService)
- Controller implementation with file upload handling
- S3 integration for file storage with retry logic
- Event publishing and notification handling
- Integration test scenarios

**Ordering Strategy**:
- Tests first (contract, integration)
- Domain model (entity, repository)
- Service layer with business logic
- Controller with file upload handling
- Event handling and notifications
- Error handling and retry mechanisms

**Estimated Output**: 20-25 numbered, ordered tasks in tasks.md

## Phase 3+: Future Implementation
*These phases are beyond the scope of the /plan command*

**Phase 3**: Task execution (/tasks command creates tasks.md)  
**Phase 4**: Implementation (execute tasks.md following constitutional principles)  
**Phase 5**: Validation (run tests, execute quickstart.md, performance validation)

## Complexity Tracking
*No constitutional violations requiring justification*

## Progress Tracking
*This checklist is updated during execution flow*

**Phase Status**:
- [x] Phase 0: Research complete (/plan command)
- [x] Phase 1: Design complete (/plan command)
- [x] Phase 2: Task planning complete (/plan command - describe approach only)
- [ ] Phase 3: Tasks generated (/tasks command)
- [ ] Phase 4: Implementation complete
- [ ] Phase 5: Validation passed

**Gate Status**:
- [x] Initial Constitution Check: PASS
- [x] Post-Design Constitution Check: PASS
- [x] All NEEDS CLARIFICATION resolved
- [x] Complexity deviations documented

---
*Based on Constitution v1.1.0 - See `.specify/memory/constitution.md`*