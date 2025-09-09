package com.shiksha.authentication.web;

import com.shiksha.authentication.domain.User;
import com.shiksha.authentication.domain.UserRole;
import com.shiksha.authentication.domain.UserService;
import com.shiksha.authentication.security.JwtTokenProvider;
import com.shiksha.authentication.web.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class
AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager,
                         UserService userService,
                         JwtTokenProvider tokenProvider,
                         PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.tokenProvider = tokenProvider;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<LoginResponse.UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        try {
            User user;
            if (request.role() == UserRole.STUDENT && request.gradeLevel() != null) {
                user = userService.createStudent(
                        request.email(),
                        request.password(),
                        request.firstName(),
                        request.lastName(),
                        request.gradeLevel()
                );
            } else {
                user = userService.createUser(
                        request.email(),
                        request.password(),
                        request.firstName(),
                        request.lastName(),
                        request.role()
                );
            }

            if (request.phone() != null) {
                user.setPhone(request.phone());
                user = userService.updateUser(user);
            }

            LoginResponse.UserResponse userResponse = new LoginResponse.UserResponse(
                    user.getId(),
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getRole()
            );

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("User registered successfully", userResponse));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage(), "VALIDATION_ERROR"));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );

            User user = (User) authentication.getPrincipal();
            String accessToken = tokenProvider.generateAccessToken(authentication);
            String refreshToken = tokenProvider.generateRefreshToken(user);

            LoginResponse.UserResponse userResponse = new LoginResponse.UserResponse(
                    user.getId(),
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getRole()
            );

            LoginResponse loginResponse = new LoginResponse(
                    accessToken,
                    refreshToken,
                    tokenProvider.getJwtExpirationInHours() * 3600,
                    userResponse
            );

            return ResponseEntity.ok(ApiResponse.success(loginResponse));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid email or password", "AUTHENTICATION_FAILED"));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(@AuthenticationPrincipal User user) {
        UserProfileResponse profile = new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                user.getRole(),
                user.getGradeLevel(),
                user.getCreatedAt()
        );

        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody UpdateProfileRequest request) {

        User updatedUser = userService.updateProfile(
                currentUser.getId(),
                request.firstName(),
                request.lastName(),
                request.phone()
        );

        UserProfileResponse profile = new UserProfileResponse(
                updatedUser.getId(),
                updatedUser.getEmail(),
                updatedUser.getFirstName(),
                updatedUser.getLastName(),
                updatedUser.getPhone(),
                updatedUser.getRole(),
                updatedUser.getGradeLevel(),
                updatedUser.getCreatedAt()
        );

        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", profile));
    }

    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody ChangePasswordRequest request) {

        try {
            // Verify current password
            if (!passwordEncoder.matches(request.currentPassword(), currentUser.getPasswordHash())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Current password is incorrect", "INVALID_PASSWORD"));
            }

            // Update password
            userService.changePassword(currentUser.getId(), request.newPassword());

            return ResponseEntity.ok(ApiResponse.success("Password changed successfully", null));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to change password", "INTERNAL_ERROR"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout() {
        // In a stateless JWT implementation, logout is typically handled client-side
        // by removing the token from storage. Server-side blacklisting could be implemented
        // for enhanced security but is not included in this basic implementation.
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully", null));
    }
}