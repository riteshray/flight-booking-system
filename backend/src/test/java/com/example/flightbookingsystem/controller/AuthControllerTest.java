package com.example.flightbookingsystem.controller;

import com.example.flightbookingsystem.dto.AuthResponse;
import com.example.flightbookingsystem.dto.LoginRequest;
import com.example.flightbookingsystem.dto.RefreshTokenRequest;
import com.example.flightbookingsystem.dto.RegisterRequest;
import com.example.flightbookingsystem.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private RefreshTokenRequest refreshTokenRequest;
    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest("john@example.com", "John Doe", "password123");
        loginRequest = new LoginRequest("john@example.com", "password123");
        refreshTokenRequest = new RefreshTokenRequest("test.refresh.token");
        
        authResponse = new AuthResponse(
                "test.access.token",
                "test.refresh.token",
                1L,
                "John Doe",
                "john@example.com",
                "Bearer"
        );
    }

    @Test
    void testRegister_WithValidRequest_ShouldReturn200() throws Exception {
        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("test.access.token"))
                .andExpect(jsonPath("$.refreshToken").value("test.refresh.token"))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void testRegister_WithInvalidEmail_ShouldReturn400() throws Exception {
        RegisterRequest invalidRequest = new RegisterRequest("invalid-email", "John Doe", "password123");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRegister_WithMissingName_ShouldReturn400() throws Exception {
        RegisterRequest invalidRequest = new RegisterRequest("john@example.com", "", "password123");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRegister_WithMissingEmail_ShouldReturn400() throws Exception {
        RegisterRequest invalidRequest = new RegisterRequest("", "John Doe", "password123");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRegister_WithDuplicateEmail_ShouldReturn400() throws Exception {
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new IllegalArgumentException("User with email already exists"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("User with email already exists"));
    }

    @Test
    void testLogin_WithValidRequest_ShouldReturn200() throws Exception {
        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("test.access.token"))
                .andExpect(jsonPath("$.refreshToken").value("test.refresh.token"))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void testLogin_WithInvalidEmail_ShouldReturn400() throws Exception {
        LoginRequest invalidRequest = new LoginRequest("invalid-email", "password123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLogin_WithNonExistentUser_ShouldReturn400() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new IllegalArgumentException("User not found"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    void testRefresh_WithValidToken_ShouldReturn200() throws Exception {
        AuthResponse newResponse = new AuthResponse(
                "new.access.token",
                "test.refresh.token",
                1L,
                "John Doe",
                "john@example.com",
                "Bearer"
        );
        when(authService.refreshToken(anyString())).thenReturn(newResponse);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshTokenRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new.access.token"))
                .andExpect(jsonPath("$.refreshToken").value("test.refresh.token"));
    }

    @Test
    void testRefresh_WithInvalidToken_ShouldReturn400() throws Exception {
        when(authService.refreshToken(anyString()))
                .thenThrow(new IllegalArgumentException("Invalid refresh token"));

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshTokenRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid refresh token"));
    }

    @Test
    void testRefresh_WithMissingToken_ShouldReturn400() throws Exception {
        RefreshTokenRequest emptyRequest = new RefreshTokenRequest("");

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyRequest)))
                .andExpect(status().isBadRequest());
    }
}
