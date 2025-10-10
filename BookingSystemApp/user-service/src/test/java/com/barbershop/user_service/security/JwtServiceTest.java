package com.barbershop.user_service.security;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.TestPropertySource;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test JWT token generation and validation
 */
@SpringBootTest
@TestPropertySource(properties = {
        "application.security.jwt.secret-key=testSecretKeyForJWTMustBe256BitsLongForHS256AlgorithmToWork",
        "application.security.jwt.expiration=900000",
        "application.security.jwt.refresh-token.expiration=604800000"
})
public class JwtServiceTest {
    @Autowired
    private JwtService jwtService;

    @Test
    void shouldGenerateAndValidateToken() {
        // Create test user
        UserDetails userDetails = User.builder()
                .username("test@example.com")
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        // Generate token
        String token = jwtService.generateToken(userDetails, 1L, "CUSTOMER");

        // Validate token
        assertNotNull(token);
        assertTrue(jwtService.isTokenValid(token, userDetails));
        assertEquals("test@example.com", jwtService.extractUsername(token));
        assertEquals(1L, jwtService.extractUserId(token));
        assertEquals("CUSTOMER", jwtService.extractRole(token));
        assertFalse(jwtService.isTokenExpired(token));
    }

    @Test
    void shouldRejectInvalidToken() {
        UserDetails userDetails = User.builder()
                .username("test@example.com")
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        String invalidToken = "invalid.jwt.token";

        assertFalse(jwtService.isTokenValid(invalidToken, userDetails));
    }
}
