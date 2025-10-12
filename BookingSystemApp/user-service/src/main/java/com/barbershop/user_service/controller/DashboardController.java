package com.barbershop.user_service.controller;

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
    public ResponseEntity<String> getCustomerDashBoard (Authentication auth){
        return ResponseEntity.ok("Welcome to your customer dashboard, " + auth.getName() + "!");
    }

    /**
     * Barber Dashboard
     */
    @GetMapping("/barber")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<String> getBarberDashboard(Authentication auth) {
        return ResponseEntity.ok("Welcome to your barber dashboard, " + auth.getName() + "!");
    }

    /**
     * Shop Owner Dashboard - SHOP_OWNER role only
     */
    @GetMapping("/owner")
    @PreAuthorize("hasRole('SHOP_OWNER')")
    public ResponseEntity<String> getOwnerDashboard(Authentication auth) {
        return ResponseEntity.ok("Welcome to your shop owner dashboard, " + auth.getName() + "!");
    }
}
