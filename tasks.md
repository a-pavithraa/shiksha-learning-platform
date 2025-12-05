# Tasks: Assignment Submission Upload

**Input**: Design documents from repository root
**Prerequisites**: assignment-submission-upload-plan.md (required), research.md, data-model.md, contracts/assignment-submission-api.yaml, quickstart.md

## Execution Flow (main)
```
1. Load assignment-submission-upload-plan.md from repository root
   → Extract: Spring Boot 3.5.5, Spring Modulith, AWS S3, PostgreSQL, Flyway
2. Load design documents:
   → data-model.md: Extract AssignmentSubmission entity → model tasks
   → contracts/assignment-submission-api.yaml: POST endpoint → contract test task
   → research.md: Extract S3 and retry decisions → setup tasks
   → quickstart.md: Extract test scenarios → integration tests
3. Generate tasks by category:
   → Setup: database migration, S3 configuration, dependencies
   → Tests: contract tests, integration tests (TDD approach)
   → Core: entity, repository, service, controller, events
   → Integration: S3 service, retry mechanism, notifications
   → Polish: validation, error handling, performance tests
4. Apply task rules:
   → Different files = mark [P] for parallel
   → Same file = sequential (no [P])
   → Tests before implementation (TDD)
5. Number tasks sequentially (T001, T002...)
6. Generate dependency graph
7. Validate task completeness
```

## Format: `[ID] [P?] Description`
- **[P]**: Can run in parallel (different files, no dependencies)
- Include exact file paths in descriptions

## Path Conventions
- **Spring Boot project**: `src/main/java/com/shiksha/`, `src/test/java/com/shiksha/`, `src/main/resources/`
- Paths shown follow Spring Modulith modular architecture

## Phase 3.1: Setup
- [x] T001 Create database migration V11__Add_assignment_submission_fields.sql in src/main/resources/db/migration/
- [x] T002 [P] Configure S3 properties in application.yml for assignment submission storage
- [x] T003 [P] Add Spring Retry dependency to pom.xml for S3 failure handling

## Phase 3.2: Tests First (TDD) ⚠️ MUST COMPLETE BEFORE 3.3
**CRITICAL: These tests MUST be written and MUST FAIL before ANY implementation**
- [x] T004 [P] Contract test POST /api/assignments/{assignmentId}/submissions in src/test/java/com/shiksha/academic/web/controller/AssignmentSubmissionControllerTest.java
- [x] T005 [P] Integration test successful assignment submission in src/test/java/com/shiksha/integration/AssignmentSubmissionIntegrationTest.java
- [x] T006 [P] Integration test late submission detection in src/test/java/com/shiksha/integration/LateSubmissionIntegrationTest.java
- [x] T007 [P] Integration test assignment resubmission in src/test/java/com/shiksha/integration/AssignmentResubmissionIntegrationTest.java
- [x] T008 [P] Integration test file validation errors in src/test/java/com/shiksha/integration/FileValidationIntegrationTest.java
- [x] T009 [P] Integration test S3 failure and retry in src/test/java/com/shiksha/integration/S3FailureRetryIntegrationTest.java

## Phase 3.3: Core Implementation (ONLY after tests are failing)
- [x] T010 [P] AssignmentSubmission entity in src/main/java/com/shiksha/academic/domain/AssignmentSubmission.java
- [x] T011 [P] AssignmentSubmissionRepository interface in src/main/java/com/shiksha/academic/domain/AssignmentSubmissionRepository.java
- [x] T012 [P] SubmitAssignmentCommand class in src/main/java/com/shiksha/academic/domain/command/SubmitAssignmentCommand.java
- [x] T013 [P] AssignmentSubmissionDto class in src/main/java/com/shiksha/academic/domain/dto/AssignmentSubmissionDto.java
- [x] T014 [P] AssignmentSubmissionCreatedEvent class in src/main/java/com/shiksha/academic/domain/event/AssignmentSubmissionCreatedEvent.java
- [x] T015 AssignmentSubmissionService class in src/main/java/com/shiksha/academic/domain/AssignmentSubmissionService.java
- [x] T016 AssignmentSubmissionController class in src/main/java/com/shiksha/academic/web/controller/AssignmentSubmissionController.java
- [x] T017 [P] SubmitAssignmentRequest DTO in src/main/java/com/shiksha/academic/web/dto/SubmitAssignmentRequest.java
- [x] T018 [P] AssignmentSubmissionResponse DTO in src/main/java/com/shiksha/academic/web/dto/AssignmentSubmissionResponse.java

## Phase 3.4: Integration
- [x] T019 [P] S3FileService with retry logic in src/main/java/com/shiksha/shared/s3/S3FileService.java
- [x] T020 [P] File validation annotations in src/main/java/com/shiksha/shared/validation/FileValidation.java
- [x] T021 [P] AssignmentSubmissionEventListener in notification module src/main/java/com/shiksha/notification/domain/AssignmentSubmissionEventListener.java
- [x] T022 Update AssignmentSubmissionService to integrate S3FileService and event publishing
- [x] T023 Update AssignmentSubmissionController to use validation annotations and error handling
- [x] T024 [P] Global exception handler for file upload errors in src/main/java/com/shiksha/shared/exception/FileUploadExceptionHandler.java

## Phase 3.5: Polish
- [x] T025 [P] Unit tests for AssignmentSubmissionService in src/test/java/com/shiksha/academic/domain/AssignmentSubmissionServiceTest.java
- [x] T026 [P] Unit tests for file validation in src/test/java/com/shiksha/shared/validation/FileValidationTest.java
- [x] T027 [P] Performance test for concurrent submissions in src/test/java/com/shiksha/performance/ConcurrentSubmissionPerformanceTest.java
- [x] T028 [P] Unit tests for S3FileService retry logic in src/test/java/com/shiksha/shared/s3/S3FileServiceTest.java
- [x] T029 Execute quickstart.md test scenarios for manual validation
- [x] T030 [P] Update CLAUDE.md with assignment submission context and new patterns

## Dependencies
- Setup (T001-T003) before tests (T004-T009)
- Tests (T004-T009) before core implementation (T010-T018)
- T010 (AssignmentSubmission entity) blocks T011 (repository)
- T012-T014 (DTOs and commands) block T015 (service)
- T015 (service) blocks T016 (controller)
- T019-T021 (integration components) before T022-T024 (service/controller updates)
- Core implementation (T010-T018) before integration (T019-T024)
- Integration (T019-T024) before polish (T025-T030)

## Parallel Example
```
# Launch T004-T009 together (contract and integration tests):
Task: "Contract test POST /api/assignments/{assignmentId}/submissions in src/test/java/com/shiksha/academic/web/controller/AssignmentSubmissionControllerTest.java"
Task: "Integration test successful assignment submission in src/test/java/com/shiksha/integration/AssignmentSubmissionIntegrationTest.java"
Task: "Integration test late submission detection in src/test/java/com/shiksha/integration/LateSubmissionIntegrationTest.java"
Task: "Integration test assignment resubmission in src/test/java/com/shiksha/integration/AssignmentResubmissionIntegrationTest.java"
Task: "Integration test file validation errors in src/test/java/com/shiksha/integration/FileValidationIntegrationTest.java"
Task: "Integration test S3 failure and retry in src/test/java/com/shiksha/integration/S3FailureRetryIntegrationTest.java"

# Launch T010-T014 together (core domain objects):
Task: "AssignmentSubmission entity in src/main/java/com/shiksha/academic/domain/AssignmentSubmission.java"
Task: "AssignmentSubmissionRepository interface in src/main/java/com/shiksha/academic/domain/AssignmentSubmissionRepository.java"
Task: "SubmitAssignmentCommand class in src/main/java/com/shiksha/academic/domain/command/SubmitAssignmentCommand.java"
Task: "AssignmentSubmissionDto class in src/main/java/com/shiksha/academic/domain/dto/AssignmentSubmissionDto.java"
Task: "AssignmentSubmissionCreatedEvent class in src/main/java/com/shiksha/academic/domain/event/AssignmentSubmissionCreatedEvent.java"
```

## Notes
- [P] tasks = different files, no dependencies
- Verify tests fail before implementing
- Follow Spring Modulith event-driven architecture
- Maintain layer separation (DTOs, Commands, no Entity exposure)
- Commit after each task

## Task Generation Rules
*Applied during task generation*

1. **From Contracts**:
   - contracts/assignment-submission-api.yaml → T004 contract test [P]
   - POST /api/assignments/{assignmentId}/submissions → T016 controller implementation
   
2. **From Data Model**:
   - AssignmentSubmission entity → T010 model creation [P]
   - Database migration required → T001 migration script
   - Event publishing → T014 event class [P]
   
3. **From Quickstart Scenarios**:
   - Each test scenario → integration tests T005-T009 [P]
   - Performance test scenario → T027 performance test [P]

4. **Ordering**:
   - Setup → Tests → Models → Services → Controllers → Integration → Polish
   - Database migration before entity creation
   - Event classes before service implementation
   - Service before controller

## Validation Checklist
*Checked before task execution*

- [x] All contracts have corresponding tests (T004)
- [x] All entities have model tasks (T010)
- [x] All tests come before implementation (T004-T009 before T010-T018)
- [x] Parallel tasks truly independent (different files)
- [x] Each task specifies exact file path
- [x] No task modifies same file as another [P] task
- [x] Spring Modulith patterns followed (events, module separation)
- [x] Layer separation enforced (DTOs, Commands, Services)