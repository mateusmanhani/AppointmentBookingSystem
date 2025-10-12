package com.barbershop.user_service.security;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT Service for token generation, validation, and management
 * Compatible with Spring Boot 3 and Spring Security 6
 */
@Service
@Slf4j
public class JwtService {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    @Value("${application.security.jwt.refresh-token.expiration}")
    private long refreshExpiration;

    /**
     * Extract username (email) from JWT token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract user ID from JWT token
     */
    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Long.class));
    }

    /**
     * Extract user role from JWT token
     */
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    /**
     * Extract specific claim from token
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token); // Get all token information
        return claimsResolver.apply(claims); //Use provide function to extract the specific information
    }

    /**
     * Generate JWT token for authenticated user
     */
    public String generateToken(UserDetails userDetails, Long userId, String role) {
        // Create a map to store information
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", userId); // Store user's database ID
        extraClaims.put("role", role); // Store user's role

        return generateToken(extraClaims, userDetails); // generate and return token
    }

    /**
     * Generate JWT token with extra claims
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    /**
     * Generate refresh token
     * A long-lived token for automatic login, without extra info and lasting 7 days
     */
    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails, refreshExpiration);
    }

    /**
     * Build JWT token with claims and expiration
     * Create an "ID card" for the user containing:
     * Username (email), user ID, User role and expiration time
     */
    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration
    ) {
        Date now = new Date(System.currentTimeMillis()); // right now
        Date expiryDate = new Date(now.getTime() + expiration); // now + expirations

        String token = Jwts.builder()
                .claims(extraClaims) // add extra claims (userID and role)
                .subject(userDetails.getUsername()) // set main subject (email)
                .issuedAt(now) // when token was created
                .expiration(expiryDate) //when it will expire
                .signWith(getSignInKey()) //sign with the secret key
                .compact(); // convert to final string

        log.debug("Generated JWT token for user: {} (expires: {})",
                userDetails.getUsername(), expiryDate);

        return token;
    }

    /**
     * Validate JWT token against user details
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            // get username from token
            final String username = extractUsername(token);
            // Check if username matches and token is not expired
            return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Check if JWT token is expired
     */
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extract expiration date from token
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extract all claims from JWT token
     */
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.error("Error parsing JWT token: {}", e.getMessage());
            throw new RuntimeException("Invalid JWT token", e);
        }
    }

    /**
     * Get signing key for JWT token
     */
    private SecretKey getSignInKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    /**
     * Get token expiration time in seconds
     */
    public long getExpirationTime() {
        return jwtExpiration / 1000; // Convert to seconds
    }
}
