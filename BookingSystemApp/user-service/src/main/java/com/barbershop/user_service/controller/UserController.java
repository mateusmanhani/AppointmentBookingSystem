package com.barbershop.user_service.controller;

import com.barbershop.user_service.dto.*;
import com.barbershop.user_service.entity.UserRole;
import com.barbershop.user_service.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for User management operations
 * Handles registration, authentication, and user management
 */
@RestController
@RequestMapping("/api/users")
@Slf4j
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService){
        this.userService = userService;
    }

    /**
     * Register a new user
     * POST /api/users/register
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> registerUser(
            @Valid @RequestBody UserRegistrationDto registrationDto
    ){
        log.info("Registration request received for email: {}", registrationDto.email());
        UserResponseDto user = userService.registerUser(registrationDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    /**
     * Authenticate user login
     * POST /api/users/login
     */
    @PostMapping("/login")
    public ResponseEntity<UserResponseDto> authenticateUser(@Valid@RequestBody LoginRequestDto loginDto){
        log.info("Login request received for email: {}", loginDto.email());
        UserResponseDto user = userService.authenticateUser(loginDto);
        return ResponseEntity.ok(user);
    }

    /**
     * JWT authentication endpoint - returns JWT tokens for secure API access
     * POST /api/users/auth/login
     */
    @PostMapping("/auth/login")
    public ResponseEntity<JwtAuthResponseDto> authenticateWithJWT (@Valid@RequestBody LoginRequestDto loginDto){
        log.info("JWT authentication request received for email: {}", loginDto.email());
        JwtAuthResponseDto authResponse = userService.authenticateAndGenerateTokens(loginDto);
        // Return success response with tokens
        return ResponseEntity.ok(authResponse);
    }

    /**
     * Refresh JWT tokens using refresh token
     * POST /api/users/auth/refresh
     */
    @PostMapping(value = "/auth/refresh", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JwtAuthResponseDto> refreshToken(
            @Valid @RequestBody RefreshTokenRequestDto refreshRequest) {
        log.info("Token refresh request received");
        JwtAuthResponseDto authResponse = userService.refreshUserTokens(refreshRequest);
        return ResponseEntity.ok(authResponse);
    }

    /**
     * Get User by ID
     * GET /api/users/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id){
        log.info("Get user request for ID: {}", id);
        UserResponseDto user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    /**
     * Get User by email
     * GET /api/users/by-email?email=user@example.com
     */
    @GetMapping("/by-email")
    public ResponseEntity<UserResponseDto> getUserByEmail(@RequestParam("email") String email){
        log.info("Get user request for email: {}", email);
        UserResponseDto user = userService.getUserByEmail(email);
        return ResponseEntity.ok(user);
    }

    /**
     * Update User profile - requires admin role
     * PUT /api/users/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUser(
            @PathVariable long id,
            @Valid@RequestBody UserUpdateDto updateDto){
        log.info("Update user request for ID: {}", id);
        UserResponseDto user = userService.updateUser(id,updateDto);
        return ResponseEntity.ok(user);
    }

    /**
     * Update Self Profile - logged-in user
     * PUT /api/users/profile // Extracts user from JWT token
     */
    @PutMapping("/profile")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('BARBER')")
    public ResponseEntity<UserResponseDto> updateSelfProfile(
            @RequestBody @Valid UserUpdateDto userUpdateDto,
            Authentication authentication) {

        String email = authentication.getName();
        UserResponseDto updated = userService.updateSelfProfile(email, userUpdateDto);
        return ResponseEntity.ok(updated);
    }

    /**
     * Step 1: Request password reset token
     * POST /api/users/forgot-password
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(
            @Valid @RequestBody ForgotPasswordDto forgotPasswordDto) {

        log.info("Password reset request received");

        userService.initiateForgotPassword(forgotPasswordDto);

        return ResponseEntity.ok(
                "If the email exists in our system, you will receive password reset instructions");
    }

    /**
     * Step 2: Reset password using token
     * POST /api/users/reset-password
     */
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
            @Valid @RequestBody ResetPasswordDto resetDto) {

        log.info("Password reset with token requested");

        userService.resetPassword(resetDto);

        return ResponseEntity.ok("Password reset successfully. You can now login with your new password.");
    }



    /**
     * Deactivate user account
     * DELETE /api/users/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateUser(@PathVariable Long id) {
        log.info("Deactivate user request for ID: {}", id);
        userService.deactivateUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Reactivate user account
     * POST /api/users/{id}/reactivate
     */
    @PostMapping("/{id}/reactivate")
    public ResponseEntity<Void> reactivateUser(@PathVariable Long id) {
        log.info("Reactivate user request for ID: {}", id);
        userService.reactivateUser(id);
        return ResponseEntity.ok().build();
    }

    // ================== SEARCH & LISTING ==================

    /**
     * Get all users with pagination
     * GET /api/users?page=0&size=10
     */
    @GetMapping
    public ResponseEntity<Page<UserResponseDto>> getAllUsers(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size){
        log.info("Get all users requests - page: {}, size: {}", page,size);
        Pageable pageable = PageRequest.of(page,size);
        Page<UserResponseDto> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    /**
     * Get users by role with pagination
     * GET /api/users/by-role/CUSTOMER?page=0&size=10
     */
    @GetMapping("/by-role/{role}")
    public ResponseEntity<Page<UserResponseDto>> getUsersByRole(
            @PathVariable("role") UserRole role,
            @RequestParam(value = "page",defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        log.info("Get users by role request - role: {}, page: {}, size: {}", role, page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<UserResponseDto> users = userService.getUsersByRole(role, pageable);
        log.info("Found {} users for role: {}", users.getTotalElements(), role);
        return ResponseEntity.ok(users);
    }

    /**
     * Search users by name
     * GET /api/users/search?name=john
     */
    @GetMapping("/search")
    public ResponseEntity<List<UserResponseDto>> searchUsers(
            @RequestParam("name") String name) {
        log.info("Search users request for name: {}", name);
        List<UserResponseDto> users = userService.searchUsersByName(name);
        return ResponseEntity.ok(users);
    }

    /**
     * Get staff members
     * GET /api/users/staff
     */
    @GetMapping("/staff")
    public ResponseEntity<List<UserResponseDto>> getStaffMembers() {
        log.info("Get staff members request");
        List<UserResponseDto> staff = userService.getStaffMembers();
        return ResponseEntity.ok(staff);
    }

    /**
     * Check if email exists
     * GET /api/users/exists?email=user@example.com
     */
    @GetMapping("/exists")
    public ResponseEntity<Boolean> checkEmailExists(
            @RequestParam("email") String email) {
        log.info("Check email exists request for: {}", email);
        boolean exists = userService.existsByEmail(email);
        return ResponseEntity.ok(exists);
    }
}
