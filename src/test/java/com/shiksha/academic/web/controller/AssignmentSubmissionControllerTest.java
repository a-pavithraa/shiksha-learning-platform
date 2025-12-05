package com.shiksha.academic.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shiksha.academic.domain.AssignmentSubmissionService;
import com.shiksha.academic.domain.dto.AssignmentSubmissionDto;
import com.shiksha.academic.domain.command.SubmitAssignmentCommand;
import com.shiksha.academic.web.controller.AssignmentSubmissionController;
import com.shiksha.authentication.UserServiceAPI;
import com.shiksha.authentication.domain.UserService;
import com.shiksha.authentication.domain.models.UserDto;
import com.shiksha.authentication.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AssignmentSubmissionController.class)
class AssignmentSubmissionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AssignmentSubmissionService assignmentSubmissionService;

    @MockitoBean
    private JwtTokenProvider tokenProvider;
    @MockitoBean
    private UserServiceAPI userServiceAPI;
    @MockitoBean
    private UserService userService;
    @Test
    @WithMockUser(roles = "STUDENT")
    void submitAssignment_ShouldReturnCreated_WhenValidPdfFile() throws Exception {
        // Given
        Long assignmentId = 1L;
        MockMultipartFile pdfFile = new MockMultipartFile(
                "file",
                "assignment.pdf",
                "application/pdf",
                "PDF content".getBytes()
        );
        
        AssignmentSubmissionDto expectedDto = new AssignmentSubmissionDto(
                123L,
                assignmentId,
                1L, // studentId
                "assignment.pdf",
                5242880L, // fileSize
                LocalDateTime.now(),
                false, // isLate
                false  // isResubmission
        );
        
        when(assignmentSubmissionService.submitAssignment(any(SubmitAssignmentCommand.class)))
                .thenReturn(expectedDto);
        when(userServiceAPI.findById(1L)).thenReturn(Optional.of(new UserDto(1L,"test@test.com","John","Doe")));
        when(userServiceAPI.findByUsername(any())).thenReturn(new UserDto(1L,"test@test.com","test","test"));
        // When & Then
        mockMvc.perform(multipart("/api/assignments/{assignmentId}/submissions", assignmentId)
                        .file(pdfFile)
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Assignment submitted successfully"))
                .andExpect(jsonPath("$.data.submissionId").value(123))
                .andExpect(jsonPath("$.data.fileName").value("assignment.pdf"))
                .andExpect(jsonPath("$.data.isLate").value(false))
                .andExpect(jsonPath("$.data.isResubmission").value(false));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void submitAssignment_ShouldReturnBadRequest_WhenNonPdfFile() throws Exception {
        // Given
        Long assignmentId = 1L;
        MockMultipartFile docFile = new MockMultipartFile(
                "file",
                "assignment.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "DOCX content".getBytes()
        );


        mockMvc.perform(multipart("/api/assignments/{assignmentId}/submissions", assignmentId)
                        .file(docFile)
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("File must be PDF format"))
                .andExpect(jsonPath("$.error").value("INVALID_FILE_TYPE"));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void submitAssignment_ShouldReturnBadRequest_WhenFileTooLarge() throws Exception {
        // Given
        Long assignmentId = 1L;
        byte[] largeContent = new byte[5242881]; // Just over 5MB
        MockMultipartFile largePdfFile = new MockMultipartFile(
                "file",
                "large_assignment.pdf",
                "application/pdf",
                largeContent
        );

        // When & Then
        mockMvc.perform(multipart("/api/assignments/{assignmentId}/submissions", assignmentId)
                        .file(largePdfFile)
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("File size exceeds 5MB limit"))
                .andExpect(jsonPath("$.error").value("FILE_TOO_LARGE"));
    }

    @Test
    void submitAssignment_ShouldReturnUnauthorized_WhenNoAuthentication() throws Exception {
        // Given
        Long assignmentId = 1L;
        MockMultipartFile pdfFile = new MockMultipartFile(
                "file",
                "assignment.pdf",
                "application/pdf",
                "PDF content".getBytes()
        );

        // When & Then
        mockMvc.perform(multipart("/api/assignments/{assignmentId}/submissions", assignmentId)
                        .file(pdfFile)
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isUnauthorized());
    }


}