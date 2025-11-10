package com.barbershop.shop_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO for creating a new employee.
 */
public record EmployeeRequestDto(
    @NotBlank(message = "Name is required")
    String name,
    
    @NotBlank(message = "Role is required")
    String role,
    
    @Email(message = "Invalid email format")
    String email,
    
    String phone
) {}
