package com.barbershop.shop_service.dto;

import jakarta.validation.constraints.Size;

/**
 * DTO for updating an existing Shop. All fields optional; controller/service validate changes.
 */
public record ShopUpdateDto(
        @Size(max = 150) String name,
        @Size(max = 300) String address,
        @Size(max = 50) String phone,
        Double latitude,
        Double longitude
) {}
