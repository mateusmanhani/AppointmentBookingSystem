package com.barbershop.user_service.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserUpdateDto(
        @Size(min = 2, max = 100, message = "First name must be between 2 and 100 characters")
        String firstName,

        @Size(min = 2, max = 100, message = "Last name must be between 2 and 100 characters")
        String lastName,

        @Pattern(
                regexp = "^[+]?[0-9]{10,15}$",
                message = "Please provide a valid phone number (10-15 digits)"
        )
        String phone,

        @Size(max = 500, message = "Address must not exceed 500 characters")
        String address
) {
    /**
     * Check if at least one field is provided for update
     */
    public boolean hasValidFields() {
        return firstName != null || lastName != null ||
                phone != null || address != null;
    }
}
