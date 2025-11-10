package com.barbershop.shop_service.controller;

import com.barbershop.shop_service.dto.EmployeeRequestDto;
import com.barbershop.shop_service.dto.EmployeeResponseDto;
import com.barbershop.shop_service.dto.EmployeeUpdateDto;
import com.barbershop.shop_service.entity.Employee;
import com.barbershop.shop_service.entity.Shop;
import com.barbershop.shop_service.service.EmployeeService;
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
 * REST controller for managing employees within a shop.
 */
@RestController
@RequestMapping("/api/shops/{shopId}/employees")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final com.barbershop.shop_service.service.ShopService shopService;

    public EmployeeController(EmployeeService employeeService, com.barbershop.shop_service.service.ShopService shopService) {
        this.employeeService = employeeService;
        this.shopService = shopService;
    }

    /**
     * Add a new employee to a shop.
     * POST /api/shops/{shopId}/employees
     * Requires SHOP_OWNER role and ownership of the shop.
     */
    @PreAuthorize("hasRole('SHOP_OWNER')")
    @PostMapping
    public ResponseEntity<EmployeeResponseDto> addEmployee(
            @PathVariable Long shopId,
            @Valid @RequestBody EmployeeRequestDto dto,
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

        // Add employee
        Employee created = employeeService.addEmployee(shopId, dto);

        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(created.getId())
            .toUri();

        EmployeeResponseDto response = new EmployeeResponseDto(
            created.getId(), created.getShopId(), created.getName(), created.getRole(),
            created.getEmail(), created.getPhone(), created.getCreatedAt(), created.getUpdatedAt());

        return ResponseEntity.created(location).body(response);
    }

    /**
     * Get all employees for a shop.
     * GET /api/shops/{shopId}/employees
     * Public endpoint.
     */
    @GetMapping
    public ResponseEntity<List<EmployeeResponseDto>> getEmployees(@PathVariable Long shopId) {
        List<Employee> employees = employeeService.getEmployeesByShopId(shopId);

        List<EmployeeResponseDto> response = employees.stream()
            .map(e -> new EmployeeResponseDto(
                e.getId(), e.getShopId(), e.getName(), e.getRole(),
                e.getEmail(), e.getPhone(), e.getCreatedAt(), e.getUpdatedAt()))
            .toList();

        return ResponseEntity.ok(response);
    }

    /**
     * Get a specific employee by id.
     * GET /api/shops/{shopId}/employees/{employeeId}
     * Public endpoint.
     */
    @GetMapping("/{employeeId}")
    public ResponseEntity<EmployeeResponseDto> getEmployeeById(
            @PathVariable Long shopId,
            @PathVariable Long employeeId) {

        Optional<Employee> employeeMaybe = employeeService.getEmployeeByIdAndShopId(employeeId, shopId);

        return employeeMaybe.map(e -> ResponseEntity.ok(new EmployeeResponseDto(
                e.getId(), e.getShopId(), e.getName(), e.getRole(),
                e.getEmail(), e.getPhone(), e.getCreatedAt(), e.getUpdatedAt())))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Update an employee's information.
     * PUT /api/shops/{shopId}/employees/{employeeId}
     * Requires SHOP_OWNER role and ownership of the shop.
     * Supports partial updates - only provided fields will be updated.
     */
    @PreAuthorize("hasRole('SHOP_OWNER')")
    @PutMapping("/{employeeId}")
    public ResponseEntity<EmployeeResponseDto> updateEmployee(
            @PathVariable Long shopId,
            @PathVariable Long employeeId,
            @Valid @RequestBody EmployeeUpdateDto dto,
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

        // Check if employee exists in this shop
        Optional<Employee> existingEmployee = employeeService.getEmployeeByIdAndShopId(employeeId, shopId);
        if (existingEmployee.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Update employee
        try {
            Employee updated = employeeService.updateEmployee(employeeId, shopId, dto);
            
            EmployeeResponseDto response = new EmployeeResponseDto(
                updated.getId(), updated.getShopId(), updated.getName(), updated.getRole(),
                updated.getEmail(), updated.getPhone(), updated.getCreatedAt(), updated.getUpdatedAt());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Delete an employee from a shop.
     * DELETE /api/shops/{shopId}/employees/{employeeId}
     * Requires SHOP_OWNER role and ownership of the shop.
     */
    @PreAuthorize("hasRole('SHOP_OWNER')")
    @DeleteMapping("/{employeeId}")
    public ResponseEntity<?> deleteEmployee(
            @PathVariable Long shopId,
            @PathVariable Long employeeId,
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

        // Delete employee
        boolean deleted = employeeService.deleteEmployee(employeeId, shopId);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
