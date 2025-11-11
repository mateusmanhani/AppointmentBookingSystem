package com.barbershop.appointment_service.dto;

import java.math.BigDecimal;

/**
 * DTO for receiving shop data from shop-service.
 * Mirrors the ShopResponseDto from shop-service.
 */
public record ShopDto(
    Long id,
    String name,
    String address,
    String city,
    String state,
    String phone,
    String openingTime,   // e.g., "09:00"
    String closingTime    // e.g., "18:00"
) {}
