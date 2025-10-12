package com.barbershop.user_service.controller;

import com.barbershop.user_service.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Simple Dashboard Controller
 */
@RestController
@RequestMapping("/api/dashboard")
@Slf4j
public class DashboardController {

    /**
     * Customer Dashboard
     */
    @GetMapping("/customer")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<String>> getCustomerDashBoard (Authentication auth){
        return ResponseEntity.ok(ApiResponse.success(
                "Customer Dashboard access granted",
                "Welcome to your customer dashboard, " + auth.getName() + "!"
        ));
    }

    /**
     * Barber Dashboard
     */
    @GetMapping("/barber")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<ApiResponse<String>> getBarberDashboard(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(
                "Barber dashboard access granted",
                "Welcome to your barber dashboard, " + auth.getName() + "!"
        ));
    }

    /**
     * Shop Owner Dashboard - SHOP_OWNER role only
     */
    @GetMapping("/owner")
    @PreAuthorize("hasRole('SHOP_OWNER')")
    public ResponseEntity<ApiResponse<String>> getOwnerDashboard(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(
                "Owner dashboard access granted",
                "Welcome to your shop owner dashboard, " + auth.getName() + "!"
        ));
    }
}
