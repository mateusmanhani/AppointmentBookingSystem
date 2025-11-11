package com.barbershop.appointment_service.dto;

/**
 * DTO for receiving user data from user-service.
 * Mirrors the UserResponseDto from user-service.
 */
public record UserDto(
    Long id,
    String email,
    String firstName,
    String lastName,
    String phone
) {
    /**
     * Get full name for display.
     */
    public String getFullName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        } else if (lastName != null) {
            return lastName;
        }
        return email; // Fallback to email if no name
    }
}
