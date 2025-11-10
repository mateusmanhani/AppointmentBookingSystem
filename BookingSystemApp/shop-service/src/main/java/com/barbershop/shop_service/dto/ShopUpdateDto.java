package com.barbershop.shop_service.dto;

import jakarta.validation.constraints.Size;

/**
 * DTO for updating an existing Shop. All fields optional; controller/service validate changes.
 */
public record ShopUpdateDto(
        @Size(max = 150) String name,
        @Size(max = 300) String address,
        @Size(max = 100) String city,
        @Size(max = 50) String state,
        @Size(max = 20) String zipCode,
        @Size(max = 20) String phone,
        @Size(max = 1000) String description,
        @Size(max = 10) String openingTime,
        @Size(max = 10) String closingTime,
        Double latitude,
        Double longitude
) {}
