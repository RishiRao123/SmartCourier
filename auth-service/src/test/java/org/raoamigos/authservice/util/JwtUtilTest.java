package org.raoamigos.authservice.util;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Phase 1 — JwtUtil Unit Tests
 * No Spring context needed; values are injected via ReflectionTestUtils.
 */
class JwtUtilTest {

    private JwtUtil jwtUtil;

    // A valid 256-bit (32-byte) Base64-encoded secret for HS256
    private static final String TEST_SECRET =
            Base64.getEncoder().encodeToString("SmartCourierSuperSecretKeyForJWT!".getBytes());
    private static final long EXPIRATION_MS = 3_600_000L; // 1 hour

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtUtil, "jwtExpiration", EXPIRATION_MS);
    }

    // =============================================
    // Scenario 1: generateToken returns a valid JWT string
    // =============================================
    @Test
    @DisplayName("generateToken(4-arg) should return a non-null, non-blank JWT")
    void generateToken_ShouldReturnValidJwt() {
        String token = jwtUtil.generateToken("user@test.com", 1L, "ROLE_CUSTOMER", "testuser");
        assertNotNull(token);
        assertFalse(token.isBlank());
        // A JWT always has 3 dot-separated parts
        assertEquals(3, token.split("\\.").length);
    }

    // =============================================
    // Scenario 2: extractEmail round-trip
    // =============================================
    @Test
    @DisplayName("extractEmail should return the original email used to generate the token")
    void extractEmail_ShouldMatchOriginalEmail() {
        String email = "rishi@test.com";
        String token = jwtUtil.generateToken(email, 2L, "ROLE_ADMIN", "rishiAdmin");
        assertEquals(email, jwtUtil.extractEmail(token));
    }

    // =============================================
    // Scenario 3: extractRole round-trip
    // =============================================
    @Test
    @DisplayName("extractRole should return the original role embedded in the token")
    void extractRole_ShouldMatchOriginalRole() {
        String token = jwtUtil.generateToken("a@b.com", 3L, "ROLE_SUPER_ADMIN", "superUser");
        assertEquals("ROLE_SUPER_ADMIN", jwtUtil.extractRole(token));
    }

    // =============================================
    // Scenario 4: extractUserId round-trip
    // =============================================
    @Test
    @DisplayName("extractUserId should return the correct Long userId from the token")
    void extractUserId_ShouldMatchOriginalUserId() {
        long expectedId = 42L;
        String token = jwtUtil.generateToken("user@example.com", expectedId, "ROLE_CUSTOMER", "someUser");
        assertEquals(expectedId, jwtUtil.extractUserId(token));
    }

    // =============================================
    // Scenario 5: validateToken throws on tampered token
    // =============================================
    @Test
    @DisplayName("validateToken should throw JwtException when the token is tampered")
    void validateToken_WhenTokenIsTampered_ShouldThrowJwtException() {
        String token = jwtUtil.generateToken("user@example.com", 1L, "ROLE_CUSTOMER", "user");
        // Corrupt the signature segment
        String tamperedToken = token.substring(0, token.lastIndexOf('.') + 1) + "invalidSignature";
        assertThrows(JwtException.class, () -> jwtUtil.validateToken(tamperedToken));
    }
}
