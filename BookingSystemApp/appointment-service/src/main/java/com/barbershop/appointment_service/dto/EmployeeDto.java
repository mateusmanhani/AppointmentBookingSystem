package com.barbershop.appointment_service.dto;

/**
 * DTO for receiving employee data from shop-service.
 * Mirrors the EmployeeResponseDto from shop-service.
 */
public record EmployeeDto(
    Long id,
    Long shopId,
    String name,
    String role,
    String email,
    String phone
) {}
