package com.barbershop.shop_service.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for creating a new Shop
 */
public record ShopRequestDto(
        @NotBlank String name,
        @NotBlank String address,
        String city,
        String state,
        String zipCode,
        String phone,
        String description,
        String openingTime,
        String closingTime,
        Double latitude,
        Double longitude
) {}
