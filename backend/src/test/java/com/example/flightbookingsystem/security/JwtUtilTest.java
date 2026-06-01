package com.example.flightbookingsystem.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private final String testSecret = "test-secret-key-minimum-32-characters-for-hmac-sha256-algorithm";
    private final Long accessTokenExpiration = 604800000L; // 7 days
    private final Long refreshTokenExpiration = 2592000000L; // 30 days
    private final Long testUserId = 1L;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", testSecret);
        ReflectionTestUtils.setField(jwtUtil, "accessTokenExpiration", accessTokenExpiration);
        ReflectionTestUtils.setField(jwtUtil, "refreshTokenExpiration", refreshTokenExpiration);
    }

    @Test
    void testGenerateAccessToken_ShouldReturnNonNullToken() {
        String token = jwtUtil.generateAccessToken(testUserId);
        
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void testGenerateRefreshToken_ShouldReturnNonNullToken() {
        String token = jwtUtil.generateRefreshToken(testUserId);
        
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void testGenerateAccessAndRefreshTokens_ShouldBeDifferent() {
        String accessToken = jwtUtil.generateAccessToken(testUserId);
        String refreshToken = jwtUtil.generateRefreshToken(testUserId);
        
        assertNotEquals(accessToken, refreshToken);
    }

    @Test
    void testValidateToken_WithValidToken_ShouldReturnTrue() {
        String token = jwtUtil.generateAccessToken(testUserId);
        
        boolean isValid = jwtUtil.validateToken(token);
        
        assertTrue(isValid);
    }

    @Test
    void testValidateToken_WithInvalidToken_ShouldReturnFalse() {
        String invalidToken = "invalid.token.here";
        
        boolean isValid = jwtUtil.validateToken(invalidToken);
        
        assertFalse(isValid);
    }

    @Test
    void testValidateToken_WithEmptyToken_ShouldReturnFalse() {
        boolean isValid = jwtUtil.validateToken("");
        
        assertFalse(isValid);
    }

    @Test
    void testExtractUserId_ShouldReturnCorrectUserId() {
        String token = jwtUtil.generateAccessToken(testUserId);
        
        Long extractedUserId = jwtUtil.extractUserId(token);
        
        assertEquals(testUserId, extractedUserId);
    }

    @Test
    void testExtractUserId_FromRefreshToken_ShouldReturnCorrectUserId() {
        String token = jwtUtil.generateRefreshToken(testUserId);
        
        Long extractedUserId = jwtUtil.extractUserId(token);
        
        assertEquals(testUserId, extractedUserId);
    }

    @Test
    void testIsTokenExpired_WithValidToken_ShouldReturnFalse() {
        String token = jwtUtil.generateAccessToken(testUserId);
        
        boolean isExpired = jwtUtil.isTokenExpired(token);
        
        assertFalse(isExpired);
    }

    @Test
    void testIsTokenExpired_WithExpiredToken_ShouldReturnTrue() {
        // Create a JwtUtil with very short expiration
        JwtUtil shortExpiryJwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(shortExpiryJwtUtil, "secret", testSecret);
        ReflectionTestUtils.setField(shortExpiryJwtUtil, "accessTokenExpiration", 1L); // 1ms
        ReflectionTestUtils.setField(shortExpiryJwtUtil, "refreshTokenExpiration", refreshTokenExpiration);
        
        String token = shortExpiryJwtUtil.generateAccessToken(testUserId);
        
        try {
            Thread.sleep(10); // Wait for token to expire
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        boolean isExpired = shortExpiryJwtUtil.isTokenExpired(token);
        
        assertTrue(isExpired);
    }

    @Test
    void testIsTokenExpired_WithInvalidToken_ShouldReturnTrue() {
        boolean isExpired = jwtUtil.isTokenExpired("invalid.token");
        
        assertTrue(isExpired);
    }

    @Test
    void testGenerateAccessToken_WithDifferentUserIds_ShouldReturnDifferentTokens() {
        String token1 = jwtUtil.generateAccessToken(1L);
        String token2 = jwtUtil.generateAccessToken(2L);
        
        assertNotEquals(token1, token2);
    }

    @Test
    void testExtractUserId_WithDifferentUsers_ShouldReturnCorrectUserIds() {
        Long userId1 = 1L;
        Long userId2 = 999L;
        
        String token1 = jwtUtil.generateAccessToken(userId1);
        String token2 = jwtUtil.generateAccessToken(userId2);
        
        assertEquals(userId1, jwtUtil.extractUserId(token1));
        assertEquals(userId2, jwtUtil.extractUserId(token2));
    }
}
