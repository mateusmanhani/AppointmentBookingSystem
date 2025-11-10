package com.barbershop.shop_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * DTO for creating a new service.
 */
public record ServiceRequestDto(
    @NotBlank(message = "Service name is required")
    String name,
    
    String description,
    
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    BigDecimal price,
    
    @NotNull(message = "Duration is required")
    @Positive(message = "Duration must be positive")
    Integer duration
) {}
