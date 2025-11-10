package com.barbershop.shop_service.dto;

import java.time.LocalDateTime;

/**
 * DTO for employee response.
 */
public record EmployeeResponseDto(
    Long id,
    Long shopId,
    String name,
    String role,
    String email,
    String phone,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
