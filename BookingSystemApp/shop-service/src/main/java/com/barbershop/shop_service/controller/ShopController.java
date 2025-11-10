package com.barbershop.shop_service.controller;

import java.net.URI;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.security.oauth2.jwt.Jwt;


import com.barbershop.shop_service.dto.ShopRequestDto;
import com.barbershop.shop_service.dto.ShopResponseDto;
import com.barbershop.shop_service.dto.ShopUpdateDto;
import com.barbershop.shop_service.entity.Shop;
import com.barbershop.shop_service.service.ShopService;

import jakarta.validation.Valid;

/**
 * REST controller for exposing shop-related endpoints.
 */
@RestController
@RequestMapping("/api/shops")
public class ShopController {

    private final ShopService shopService;

    public ShopController(ShopService shopService) {
        this.shopService = shopService;
    }

    /**
     * Create a new shop.
     * Returns 201 Created with Location header pointing to the new resource.
     */
    @PreAuthorize("hasRole('SHOP_OWNER')")
    @PostMapping
    public ResponseEntity<ShopResponseDto> createShop(@Valid @RequestBody ShopRequestDto dto, org.springframework.security.core.Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        Long currentUserId = jwt.getClaim("userId");

        Shop created = shopService.createFromDto(dto, currentUserId);
        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(created.getId())
            .toUri();

        ShopResponseDto response = toResponseDto(created);

        return ResponseEntity.created(location).body(response);
    }

    /**
     * Retrieve shop by id.
     * Returns 200 OK with shop or 404 Not Found when absent.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ShopResponseDto> getShopById(@PathVariable Long id) {
        Optional<Shop> maybe = shopService.getShopById(id);
        return maybe.map(shop -> ResponseEntity.ok(toResponseDto(shop)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Get paginated list of shops.
     * Example: GET /api/shops?page=0&size=20
     */
    @GetMapping
    public ResponseEntity<?> listShops(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

    Page<Shop> result = shopService.findAll(PageRequest.of(page, size));
    Page<ShopResponseDto> dtoPage = result.map(this::toResponseDto);

    return ResponseEntity.ok(dtoPage);
    }

    /**
     * Get list of shops owned by the authenticated user.
     * Example: GET /api/shops/my-shops
     * Requires SHOP_OWNER role and valid JWT token.
     */
    @PreAuthorize("hasRole('SHOP_OWNER')")
    @GetMapping("/my-shops")
    public ResponseEntity<?> getMyShops(org.springframework.security.core.Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        Long currentUserId = jwt.getClaim("userId");

        java.util.List<Shop> myShops = shopService.getShopsByOwnerId(currentUserId);
        java.util.List<ShopResponseDto> responseDtos = myShops.stream()
            .map(this::toResponseDto)
            .toList();

        return ResponseEntity.ok(responseDtos);
    }

    /**
     * Update a shop by id using UpdateShopDTO.
     * Returns 200 OK with updated shop, 404 Not Found when the shop does not exist,
     * or 403 Forbidden if the user is not the owner of the shop.
     */
    @PreAuthorize("hasRole('SHOP_OWNER')")
    @PutMapping("/{id}")
    public ResponseEntity<ShopResponseDto> updateShop(
            @PathVariable Long id,
            @Valid @RequestBody ShopUpdateDto dto,
            org.springframework.security.core.Authentication authentication) {

        // Get the authenticated user's ID from JWT
        Jwt jwt = (Jwt) authentication.getPrincipal();
        Long currentUserId = jwt.getClaim("userId");

        // Check if shop exists
        Optional<Shop> maybe = shopService.getShopById(id);
        if (maybe.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Verify ownership - only the owner can update their shop
        Shop shop = maybe.get();
        if (!shop.getOwnerId().equals(currentUserId)) {
            return ResponseEntity.status(403).build(); // Forbidden
        }

        Shop updated = shopService.updateFromDto(id, dto);
        ShopResponseDto response = toResponseDto(updated);

        return ResponseEntity.ok(response);
    }

    /**
     * Delete a shop by id.
     * Returns 204 No Content when deleted, 404 Not Found when not present,
     * or 403 Forbidden if the user is not the owner of the shop.
     */
    @PreAuthorize("hasRole('SHOP_OWNER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteShop(
            @PathVariable Long id,
            org.springframework.security.core.Authentication authentication) {
        
        // Get the authenticated user's ID from JWT
        Jwt jwt = (Jwt) authentication.getPrincipal();
        Long currentUserId = jwt.getClaim("userId");

        // Check if shop exists
        Optional<Shop> maybe = shopService.getShopById(id);
        if (maybe.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Verify ownership - only the owner can delete their shop
        Shop shop = maybe.get();
        if (!shop.getOwnerId().equals(currentUserId)) {
            return ResponseEntity.status(403).build(); // Forbidden
        }

        shopService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Helper method to convert Shop entity to ShopResponseDto
     */
    private ShopResponseDto toResponseDto(Shop shop) {
        return new ShopResponseDto(
            shop.getId(),
            shop.getName(),
            shop.getAddress(),
            shop.getCity(),
            shop.getState(),
            shop.getZipCode(),
            shop.getPhone(),
            shop.getDescription(),
            shop.getOpeningTime(),
            shop.getClosingTime(),
            shop.getOwnerId(),
            shop.getLatitude(),
            shop.getLongitude(),
            shop.getCreatedAt(),
            shop.getUpdatedAt()
        );
    }
}
