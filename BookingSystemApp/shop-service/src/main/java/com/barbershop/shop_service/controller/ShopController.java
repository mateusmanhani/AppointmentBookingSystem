package com.barbershop.shop_service.controller;

import java.net.URI;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
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
    @PostMapping
    public ResponseEntity<ShopResponseDto> createShop(@Valid @RequestBody ShopRequestDto dto) {
    Shop created = shopService.createFromDto(dto);
    URI location = ServletUriComponentsBuilder
        .fromCurrentRequest()
        .path("/{id}")
        .buildAndExpand(created.getId())
        .toUri();

    ShopResponseDto response = new ShopResponseDto(
        created.getId(), created.getName(), created.getAddress(), created.getOwnerId(),
        created.getLatitude(), created.getLongitude(), created.getCreatedAt(), created.getUpdatedAt());

    return ResponseEntity.created(location).body(response);
    }

    /**
     * Retrieve shop by id.
     * Returns 200 OK with shop or 404 Not Found when absent.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ShopResponseDto> getShopById(@PathVariable Long id) {
        Optional<Shop> maybe = shopService.getShopById(id);
        return maybe.map(shop -> ResponseEntity.ok(new ShopResponseDto(
                        shop.getId(), shop.getName(), shop.getAddress(), shop.getOwnerId(),
                        shop.getLatitude(), shop.getLongitude(), shop.getCreatedAt(), shop.getUpdatedAt())))
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
    Page<ShopResponseDto> dtoPage = result.map(shop -> new ShopResponseDto(
        shop.getId(), shop.getName(), shop.getAddress(), shop.getOwnerId(),
        shop.getLatitude(), shop.getLongitude(), shop.getCreatedAt(), shop.getUpdatedAt()));

    return ResponseEntity.ok(dtoPage);
    }

    /**
     * Update a shop by id using UpdateShopDTO.
     * Returns 200 OK with updated shop or 404 Not Found when the shop does not exist.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ShopResponseDto> updateShop(
            @PathVariable Long id,
            @Valid @RequestBody ShopUpdateDto dto) {

        Optional<Shop> maybe = shopService.getShopById(id);
        if (maybe.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Shop updated = shopService.updateFromDto(id, dto);
        ShopResponseDto response = new ShopResponseDto(
                updated.getId(), updated.getName(), updated.getAddress(), updated.getOwnerId(),
                updated.getLatitude(), updated.getLongitude(), updated.getCreatedAt(), updated.getUpdatedAt());

        return ResponseEntity.ok(response);
    }

    /**
     * Delete a shop by id.
     * Returns 204 No Content when deleted, 404 Not Found when not present.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteShop(@PathVariable Long id) {
        boolean deleted = shopService.deleteById(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * DTO used to update a shop. Place here for convenience; you can move to its own file.
     */
    // Update DTO moved to dto.ShopUpdateDto
}
