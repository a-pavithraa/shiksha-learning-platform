package com.shiksha.authentication.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shiksha.authentication.domain.User;
import com.shiksha.authentication.domain.UserRole;
import com.shiksha.authentication.domain.UserService;
import com.shiksha.authentication.security.JwtTokenProvider;
import com.shiksha.authentication.web.dto.LoginRequest;
import com.shiksha.authentication.web.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtTokenProvider tokenProvider;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @Test
    @WithMockUser(roles = "ADMIN")
    void register_ShouldCreateUser_WhenValidRequest() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "teacher@example.com",
                "password123",
                "John",
                "Doe",
                "+1234567890",
                UserRole.TEACHER,
                null
        );

        User mockUser = new User("teacher@example.com", "hashedPassword", "John", "Doe", UserRole.TEACHER);
        mockUser.setId(1L);

        when(userService.createUser(anyString(), anyString(), anyString(), anyString(), any(UserRole.class)))
                .thenReturn(mockUser);

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.data.email").value("teacher@example.com"))
                .andExpect(jsonPath("$.data.firstName").value("John"))
                .andExpect(jsonPath("$.data.lastName").value("Doe"))
                .andExpect(jsonPath("$.data.role").value("TEACHER"));
    }

    @Test
    void register_ShouldReturnUnauthorized_WhenNoAuthentication() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "teacher@example.com",
                "password123",
                "John",
                "Doe",
                "+1234567890",
                UserRole.TEACHER,
                null
        );

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void register_ShouldReturnForbidden_WhenStudentRole() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "teacher@example.com",
                "password123",
                "John",
                "Doe",
                "+1234567890",
                UserRole.TEACHER,
                null
        );

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void login_ShouldReturnBadRequest_WhenInvalidEmail() throws Exception {
        LoginRequest request = new LoginRequest("invalid-email", "password123");

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_ShouldReturnBadRequest_WhenEmptyPassword() throws Exception {
        LoginRequest request = new LoginRequest("user@example.com", "");

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}