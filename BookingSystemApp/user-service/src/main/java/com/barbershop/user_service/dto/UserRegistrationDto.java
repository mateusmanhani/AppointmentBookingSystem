package com.barbershop.user_service.dto;

import com.barbershop.user_service.entity.UserRole;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;

public record UserRegistrationDto(
        @NotBlank(message = "Email is required")
        @Email(message = "Please provide a valid email address")
        @Size(max = 255, message = "Email must not exceed 255 characters")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 50, message = "Password must be between 8 and 50 characters")
        @Pattern(
                regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!*]).*$",
                message = "Password must contain at least one digit, lowercase letter, uppercase letter, and special character"
        )
        String password,

        @NotNull(message = "User role is required")
        UserRole role,

        @NotBlank(message = "First name is required")
        @Size(min = 2, max = 100, message = "First name must be between 2 and 100 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(min = 2, max = 100, message = "Last name must be between 2 and 100 characters")
        String lastName,

        @Pattern(
                regexp = "^[+]?[0-9]{10,15}$",
                message = "Please provide a valid phone number (10-15 digits)"
        )
        String phone

) {
    @JsonIgnore
    public boolean isValidForRole() {
        return switch (role) {
            case CUSTOMER -> phone != null && !phone.isBlank();
            case STAFF, SHOP_OWNER ->
                    phone != null && !phone.isBlank();
        };
    }

    /**
     * Generate display name for user
     */
    @JsonIgnore
    public String getFullName() {
        return String.format("%s %s", firstName, lastName);
    }
}
