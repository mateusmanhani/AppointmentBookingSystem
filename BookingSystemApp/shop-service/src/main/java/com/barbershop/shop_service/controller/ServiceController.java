package com.barbershop.shop_service.controller;

import com.barbershop.shop_service.dto.ServiceRequestDto;
import com.barbershop.shop_service.dto.ServiceResponseDto;
import com.barbershop.shop_service.dto.ServiceUpdateDto;
import com.barbershop.shop_service.entity.Shop;
import com.barbershop.shop_service.entity.ShopService;
import com.barbershop.shop_service.service.ServiceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing services within a shop.
 */
@RestController
@RequestMapping("/api/shops/{shopId}/services")
public class ServiceController {

    private final ServiceService serviceService;
    private final com.barbershop.shop_service.service.ShopService shopService;

    public ServiceController(ServiceService serviceService, com.barbershop.shop_service.service.ShopService shopService) {
        this.serviceService = serviceService;
        this.shopService = shopService;
    }

    /**
     * Create a new service for a shop.
     * POST /api/shops/{shopId}/services
     * Requires SHOP_OWNER role and ownership of the shop.
     */
    @PreAuthorize("hasRole('SHOP_OWNER')")
    @PostMapping
    public ResponseEntity<ServiceResponseDto> createService(
            @PathVariable Long shopId,
            @Valid @RequestBody ServiceRequestDto dto,
            Authentication authentication) {

        // Verify shop ownership
        Jwt jwt = (Jwt) authentication.getPrincipal();
        Long currentUserId = jwt.getClaim("userId");

        Optional<Shop> shopMaybe = shopService.getShopById(shopId);
        if (shopMaybe.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Shop shop = shopMaybe.get();
        if (!shop.getOwnerId().equals(currentUserId)) {
            return ResponseEntity.status(403).build(); // Forbidden
        }

        // Create service
        ShopService created = serviceService.createService(shopId, dto);

        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(created.getId())
            .toUri();

        ServiceResponseDto response = new ServiceResponseDto(
            created.getId(), created.getShopId(), created.getName(), created.getDescription(),
            created.getPrice(), created.getDuration(), created.getCreatedAt(), created.getUpdatedAt());

        return ResponseEntity.created(location).body(response);
    }

    /**
     * Get all services for a shop.
     * GET /api/shops/{shopId}/services
     * Public endpoint.
     */
    @GetMapping
    public ResponseEntity<List<ServiceResponseDto>> getServices(@PathVariable Long shopId) {
        List<ShopService> services = serviceService.getServicesByShopId(shopId);

        List<ServiceResponseDto> response = services.stream()
            .map(s -> new ServiceResponseDto(
                s.getId(), s.getShopId(), s.getName(), s.getDescription(),
                s.getPrice(), s.getDuration(), s.getCreatedAt(), s.getUpdatedAt()))
            .toList();

        return ResponseEntity.ok(response);
    }

    /**
     * Get a specific service by id.
     * GET /api/shops/{shopId}/services/{serviceId}
     * Public endpoint.
     */
    @GetMapping("/{serviceId}")
    public ResponseEntity<ServiceResponseDto> getServiceById(
            @PathVariable Long shopId,
            @PathVariable Long serviceId) {

        Optional<ShopService> serviceMaybe = serviceService.getServiceByIdAndShopId(serviceId, shopId);

        return serviceMaybe.map(s -> ResponseEntity.ok(new ServiceResponseDto(
                s.getId(), s.getShopId(), s.getName(), s.getDescription(),
                s.getPrice(), s.getDuration(), s.getCreatedAt(), s.getUpdatedAt())))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Update a service.
     * PUT /api/shops/{shopId}/services/{serviceId}
     * Requires SHOP_OWNER role and ownership of the shop.
     */
    @PreAuthorize("hasRole('SHOP_OWNER')")
    @PutMapping("/{serviceId}")
    public ResponseEntity<ServiceResponseDto> updateService(
            @PathVariable Long shopId,
            @PathVariable Long serviceId,
            @Valid @RequestBody ServiceUpdateDto dto,
            Authentication authentication) {

        // Verify shop ownership
        Jwt jwt = (Jwt) authentication.getPrincipal();
        Long currentUserId = jwt.getClaim("userId");

        Optional<Shop> shopMaybe = shopService.getShopById(shopId);
        if (shopMaybe.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Shop shop = shopMaybe.get();
        if (!shop.getOwnerId().equals(currentUserId)) {
            return ResponseEntity.status(403).build(); // Forbidden
        }

        // Check if service exists
        Optional<ShopService> serviceMaybe = serviceService.getServiceByIdAndShopId(serviceId, shopId);
        if (serviceMaybe.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Update service
        ShopService updated = serviceService.updateService(serviceId, shopId, dto);

        ServiceResponseDto response = new ServiceResponseDto(
            updated.getId(), updated.getShopId(), updated.getName(), updated.getDescription(),
            updated.getPrice(), updated.getDuration(), updated.getCreatedAt(), updated.getUpdatedAt());

        return ResponseEntity.ok(response);
    }

    /**
     * Delete a service.
     * DELETE /api/shops/{shopId}/services/{serviceId}
     * Requires SHOP_OWNER role and ownership of the shop.
     */
    @PreAuthorize("hasRole('SHOP_OWNER')")
    @DeleteMapping("/{serviceId}")
    public ResponseEntity<?> deleteService(
            @PathVariable Long shopId,
            @PathVariable Long serviceId,
            Authentication authentication) {

        // Verify shop ownership
        Jwt jwt = (Jwt) authentication.getPrincipal();
        Long currentUserId = jwt.getClaim("userId");

        Optional<Shop> shopMaybe = shopService.getShopById(shopId);
        if (shopMaybe.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Shop shop = shopMaybe.get();
        if (!shop.getOwnerId().equals(currentUserId)) {
            return ResponseEntity.status(403).build(); // Forbidden
        }

        // Delete service
        boolean deleted = serviceService.deleteService(serviceId, shopId);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
