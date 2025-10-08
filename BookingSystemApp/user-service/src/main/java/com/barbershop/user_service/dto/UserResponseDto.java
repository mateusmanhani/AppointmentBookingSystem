package com.barbershop.user_service.dto;

import com.barbershop.user_service.entity.UserRole;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record UserResponseDto(
        Long id,
        String email,
        UserRole role,
        String firstName,
        String lastName,
        String phone,
        Boolean isActive,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime updatedAt
) {

    /**
     * Get full name for display purposes
     */
    public String getFullName() {
        return String.format("%s %s", firstName, lastName);
    }

    /**
     * Check if user profile is complete
     */
    public boolean isProfileComplete() {
        return firstName != null && lastName != null &&
                phone != null && email != null;
    }
}
