package com.example.flightbookingsystem.service;

import com.example.flightbookingsystem.dto.AuthResponse;
import com.example.flightbookingsystem.dto.LoginRequest;
import com.example.flightbookingsystem.dto.RegisterRequest;
import com.example.flightbookingsystem.model.User;
import com.example.flightbookingsystem.repository.UserRepository;
import com.example.flightbookingsystem.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private final String testAccessToken = "test.access.token";
    private final String testRefreshToken = "test.refresh.token";

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("John Doe");
        testUser.setEmail("john@example.com");
        testUser.setPassword("$2a$10$slYQmyNdGzTn7ZLHaNCB/.XNrLEfvIxJYAZOe6wHs8rU6H1rNEyPS"); // BCrypt hash of "password123"

        registerRequest = new RegisterRequest();
        registerRequest.setEmail("john@example.com");
        registerRequest.setName("John Doe");
        registerRequest.setPassword("password123");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("john@example.com");
        loginRequest.setPassword("password123");
    }

    @Test
    void testRegister_WithNewUser_ShouldReturnAuthResponse() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtUtil.generateAccessToken(testUser.getId())).thenReturn(testAccessToken);
        when(jwtUtil.generateRefreshToken(testUser.getId())).thenReturn(testRefreshToken);

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals(testAccessToken, response.getAccessToken());
        assertEquals(testRefreshToken, response.getRefreshToken());
        assertEquals(testUser.getId(), response.getUserId());
        assertEquals(testUser.getName(), response.getName());
        assertEquals(testUser.getEmail(), response.getEmail());
        assertEquals("Bearer", response.getTokenType());

        verify(userRepository).existsByEmail(registerRequest.getEmail());
        verify(userRepository).save(any(User.class));
        verify(jwtUtil).generateAccessToken(testUser.getId());
        verify(jwtUtil).generateRefreshToken(testUser.getId());
    }

    @Test
    void testRegister_WithExistingEmail_ShouldThrowException() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> authService.register(registerRequest));

        assertTrue(exception.getMessage().contains("already exists"));
        verify(userRepository).existsByEmail(registerRequest.getEmail());
        verify(userRepository, never()).save(any(User.class));
        verify(jwtUtil, never()).generateAccessToken(any());
    }

    @Test
    void testLogin_WithExistingUser_ShouldReturnAuthResponse() {
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(jwtUtil.generateAccessToken(testUser.getId())).thenReturn(testAccessToken);
        when(jwtUtil.generateRefreshToken(testUser.getId())).thenReturn(testRefreshToken);

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals(testAccessToken, response.getAccessToken());
        assertEquals(testRefreshToken, response.getRefreshToken());
        assertEquals(testUser.getId(), response.getUserId());
        assertEquals(testUser.getName(), response.getName());
        assertEquals(testUser.getEmail(), response.getEmail());
        assertEquals("Bearer", response.getTokenType());

        verify(userRepository).findByEmail(loginRequest.getEmail());
        verify(jwtUtil).generateAccessToken(testUser.getId());
        verify(jwtUtil).generateRefreshToken(testUser.getId());
    }

    @Test
    void testLogin_WithNonExistentUser_ShouldThrowException() {
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> authService.login(loginRequest));

        assertTrue(exception.getMessage().contains("not found"));
        verify(userRepository).findByEmail(loginRequest.getEmail());
        verify(jwtUtil, never()).generateAccessToken(any());
    }

    @Test
    void testRefreshToken_WithValidToken_ShouldReturnNewAccessToken() {
        when(jwtUtil.validateToken(testRefreshToken)).thenReturn(true);
        when(jwtUtil.extractUserId(testRefreshToken)).thenReturn(testUser.getId());
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(jwtUtil.generateAccessToken(testUser.getId())).thenReturn("new.access.token");

        AuthResponse response = authService.refreshToken(testRefreshToken);

        assertNotNull(response);
        assertEquals("new.access.token", response.getAccessToken());
        assertEquals(testRefreshToken, response.getRefreshToken()); // Same refresh token
        assertEquals(testUser.getId(), response.getUserId());
        assertEquals(testUser.getName(), response.getName());
        assertEquals(testUser.getEmail(), response.getEmail());

        verify(jwtUtil).validateToken(testRefreshToken);
        verify(jwtUtil).extractUserId(testRefreshToken);
        verify(userRepository).findById(testUser.getId());
        verify(jwtUtil).generateAccessToken(testUser.getId());
    }

    @Test
    void testRefreshToken_WithInvalidToken_ShouldThrowException() {
        when(jwtUtil.validateToken(anyString())).thenReturn(false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.refreshToken("invalid.token")
        );

        assertTrue(exception.getMessage().contains("Invalid refresh token"));
        verify(jwtUtil).validateToken(anyString());
        verify(jwtUtil, never()).extractUserId(anyString());
    }

    @Test
    void testRefreshToken_WithNonExistentUser_ShouldThrowException() {
        when(jwtUtil.validateToken(testRefreshToken)).thenReturn(true);
        when(jwtUtil.extractUserId(testRefreshToken)).thenReturn(999L);
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.refreshToken(testRefreshToken)
        );

        assertTrue(exception.getMessage().contains("User not found"));
        verify(userRepository).findById(999L);
    }
}
