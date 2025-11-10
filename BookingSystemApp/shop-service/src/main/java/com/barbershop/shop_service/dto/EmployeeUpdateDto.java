package com.barbershop.shop_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * DTO for updating an existing employee.
 * All fields are optional to support partial updates.
 */
public record EmployeeUpdateDto(
    @Size(min = 1, max = 100, message = "Name must be between 1 and 100 characters")
    String name,
    
    @Size(min = 1, max = 100, message = "Role must be between 1 and 100 characters")
    String role,
    
    @Email(message = "Invalid email format")
    String email,
    
    @Size(max = 20, message = "Phone must not exceed 20 characters")
    String phone
) {}
