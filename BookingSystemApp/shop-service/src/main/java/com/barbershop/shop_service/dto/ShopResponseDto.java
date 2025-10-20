package com.barbershop.shop_service.dto;

import java.time.LocalDateTime;

/**
 * DTO returned to clients for shop data
 */
public record ShopResponseDto(
        Long id,
        String name,
        String address,
        Long ownerId,
        Double latitude,
        Double longitude,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
