package com.barbershop.shop_service.dto;

import java.time.LocalDateTime;

/**
 * DTO returned to clients for shop data
 */
public record ShopResponseDto(
        Long id,
        String name,
        String address,
        String city,
        String state,
        String zipCode,
        String phone,
        String description,
        String openingTime,
        String closingTime,
        Long ownerId,
        Double latitude,
        Double longitude,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
