package com.barbershop.appointment_service.dto;

import java.math.BigDecimal;

/**
 * DTO representing a service offered by a shop.
 * Used to fetch service details (especially duration) from shop-service.
 * 
 * This is a simplified version containing only the fields needed for appointment booking.
 */
public record ShopServiceDto(
    Long id,
    String name,
    String description,
    BigDecimal price,
    Integer duration  // Duration in minutes - critical for slot calculation
) {}
