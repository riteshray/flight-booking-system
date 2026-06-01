package com.example.flightbookingsystem.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private final String validToken = "valid.jwt.token";
    private final Long userId = 1L;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testDoFilterInternal_WithValidToken_ShouldSetAuthentication() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(request.getRequestURI()).thenReturn("/api/v1/flights");
        when(jwtUtil.validateToken(validToken)).thenReturn(true);
        when(jwtUtil.extractUserId(validToken)).thenReturn(userId);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals(userId, authentication.getPrincipal());
        assertTrue(authentication.getAuthorities().isEmpty());

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_WithInvalidToken_ShouldNotSetAuthentication() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid.token");
        when(request.getRequestURI()).thenReturn("/api/v1/flights");
        when(jwtUtil.validateToken("invalid.token")).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);

        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).extractUserId(anyString());
    }

    @Test
    void testDoFilterInternal_WithNoToken_ShouldNotSetAuthentication() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getRequestURI()).thenReturn("/api/v1/flights");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);

        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).validateToken(anyString());
    }

    @Test
    void testDoFilterInternal_WithMalformedAuthorizationHeader_ShouldNotSetAuthentication() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("InvalidHeader");
        when(request.getRequestURI()).thenReturn("/api/v1/flights");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);

        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).validateToken(anyString());
    }

    @Test
    void testShouldNotFilter_WithAuthEndpoint_ShouldReturnTrue() {
        when(request.getRequestURI()).thenReturn("/api/v1/auth/register");

        boolean shouldNotFilter = jwtAuthenticationFilter.shouldNotFilter(request);

        assertTrue(shouldNotFilter);
    }

    @Test
    void testShouldNotFilter_WithAuthLoginEndpoint_ShouldReturnTrue() {
        when(request.getRequestURI()).thenReturn("/api/v1/auth/login");

        boolean shouldNotFilter = jwtAuthenticationFilter.shouldNotFilter(request);

        assertTrue(shouldNotFilter);
    }

    @Test
    void testShouldNotFilter_WithProtectedEndpoint_ShouldReturnFalse() {
        when(request.getRequestURI()).thenReturn("/api/v1/flights");

        boolean shouldNotFilter = jwtAuthenticationFilter.shouldNotFilter(request);

        assertFalse(shouldNotFilter);
    }

    @Test
    void testDoFilterInternal_WithTokenException_ShouldContinueFilterChain() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(request.getRequestURI()).thenReturn("/api/v1/flights");
        when(jwtUtil.validateToken(validToken)).thenThrow(new RuntimeException("Token parsing error"));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_WithEmptyBearerToken_ShouldNotSetAuthentication() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer ");
        when(request.getRequestURI()).thenReturn("/api/v1/flights");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);

        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).validateToken(anyString());
    }
}
