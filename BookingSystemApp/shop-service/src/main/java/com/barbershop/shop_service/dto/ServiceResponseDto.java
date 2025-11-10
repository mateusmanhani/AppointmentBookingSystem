package com.barbershop.shop_service.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for service response.
 */
public record ServiceResponseDto(
    Long id,
    Long shopId,
    String name,
    String description,
    BigDecimal price,
    Integer duration,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
