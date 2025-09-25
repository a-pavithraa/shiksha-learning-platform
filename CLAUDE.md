# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot Learning Management System (LMS) called Shiksha LMS. It's a Java 21 application using Spring Boot 3.5.5 with Spring Security, PostgreSQL database, and Flyway for database migrations.

## Key Technologies

- **Framework**: Spring Boot 3.5.5 with Spring Modulith for modular architecture
- **Language**: Java 21
- **Database**: PostgreSQL with Flyway migrations
- **Security**: Spring Security
- **Testing**: JUnit 5 with Testcontainers for integration testing
- **Build Tool**: Maven

## Common Commands

### Build and Run
```bash
# Build the project
./mvnw clean compile

# Run the application
./mvnw spring-boot:run

# Package the application
./mvnw clean package
```

### Testing
```bash
# Run all tests
./mvnw test

# Run a specific test class
./mvnw test -Dtest=ShikshaLmsApplicationTests

# Run tests with coverage
./mvnw test jacoco:report
```

### Development
```bash
# Clean and install dependencies
./mvnw clean install

# Run with development profile (if configured)
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

## Architecture

### Package Structure
- `com.shiksha` - Root package for the application
- `src/main/resources/db/migration/` - Flyway database migration scripts
- `src/test/java/com/shiksha/` - Test classes with Testcontainers configuration

### Key Components

1. **ShikshaLmsApplication** - Main Spring Boot application class
2. **TestcontainersConfiguration** - Test configuration for PostgreSQL container using Testcontainers
3. **Database Migrations** - Located in `src/main/resources/db/migration/` (managed by Flyway)

### Testing Strategy
The project uses Testcontainers for integration testing with a real PostgreSQL database. The `TestcontainersConfiguration` class provides a PostgreSQL container that's automatically managed during tests.

## Database

- **Database**: PostgreSQL
- **Migration Tool**: Flyway
- **Test Database**: Testcontainers-managed PostgreSQL container

## Security

The application includes Spring Security. Configuration and implementation details should be found in the security-related classes when they are implemented.

## Coding Standards and Best Practices

### Data Transfer Objects (DTOs) and Command Objects

**IMPORTANT**: The application follows strict separation of concerns between layers:

#### Service Layer Best Practices
- **NEVER return Entity objects directly from Service classes**
- Service methods should return DTOs or domain-specific objects
- Entity objects should remain within the domain layer and not be exposed to controllers
- Use mapping methods or constructors to convert Entities to DTOs before returning from services

#### Controller Layer Best Practices
- **NEVER pass request objects directly to Service methods**
- Convert request objects to Command objects before passing to services
- Use Command objects to encapsulate all parameters needed for service operations
- Return Response DTOs to the client, never Entity objects

#### Layer Communication Pattern
```
Controller ──→ Command Object ──→ Service ──→ DTO ──→ Controller ──→ Response DTO
     ↑                                                                    ↓
Request DTO                                                         Client Response
```

#### Implementation Examples

**Good Pattern (Controller):**
```java
@PostMapping
public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody CreateUserRequest request) {
    CreateUserCommand command = CreateUserCommand.from(request);
    UserDto userDto = userService.createUser(command);
    UserResponse response = new UserResponse(userDto);
    return ResponseEntity.ok(ApiResponse.success(response));
}
```

**Good Pattern (Service):**
```java
public UserDto createUser(CreateUserCommand command) {
    User entity = new User(command.email(), command.firstName(), ...);
    User savedEntity = userRepository.save(entity);
    return mapToDto(savedEntity); // Return DTO, not Entity
}
```

**Bad Pattern (Avoid):**
```java
// ❌ DON'T: Service returning Entity
public User createUser(CreateUserRequest request) { ... }

// ❌ DON'T: Controller passing Request directly to Service
userService.createUser(request);
```

#### Package Structure for DTOs and Commands
- `*.web.dto` - Request/Response DTOs for web layer
- `*.domain.command` - Command objects for service layer input
- `*.domain.models` - DTOs for cross-module communication