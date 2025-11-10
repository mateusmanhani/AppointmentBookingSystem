package com.barbershop.shop_service.dto;

import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * DTO for updating an existing service.
 */
public record ServiceUpdateDto(
    String name,
    String description,
    
    @Positive(message = "Price must be positive")
    BigDecimal price,
    
    @Positive(message = "Duration must be positive")
    Integer duration
) {}
