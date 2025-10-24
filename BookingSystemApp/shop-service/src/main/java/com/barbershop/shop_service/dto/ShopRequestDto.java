package com.barbershop.shop_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for creating a new Shop
 */
public record ShopRequestDto(
        @NotBlank String name,
        @NotBlank String address,
        @NotNull Long ownerId,
        Double latitude,
        Double longitude
) {}
