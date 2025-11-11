package com.barbershop.appointment_service.dto;

import java.math.BigDecimal;

/**
 * DTO for receiving service data from shop-service.
 * Mirrors the ServiceResponseDto from shop-service.
 */
public record ServiceDto(
    Long id,
    Long shopId,
    String name,
    String description,
    BigDecimal price,
    Integer duration  // in minutes
) {}
