package com.barbershop.user_service.dto;

/**
 * JWT Authentication Response containing tokens and user information
 * Industry-standard response format for JWT authentication
 */
public record JwtAuthResponseDto(
        String accessToken,
        String tokenType,
        Long expiresIn,
        String refreshToken,
        UserResponseDto user
) {
    /**
     * Create JWT response with Bearer token type
     */
    public static JwtAuthResponseDto create(
            String accessToken,
            String refreshToken,
            Long expiresIn,
            UserResponseDto user) {
        return new JwtAuthResponseDto(
                accessToken,
                "Bearer",
                expiresIn,
                refreshToken,
                user
        );
    }

}
