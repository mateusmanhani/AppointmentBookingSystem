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
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<ApiResponse<UserResponseDto>> registerUser(
            @Valid @RequestBody UserRegistrationDto registrationDto
            ){
        log.info("Registration request received for email: {}", registrationDto.email());

        UserResponseDto user = userService.registerUser(registrationDto);

        ApiResponse<UserResponseDto> response = ApiResponse.success("User registered successfully", user);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Authenticate user login
     * POST /api/users/login
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserResponseDto>> authenticateUser(@Valid@RequestBody LoginRequestDto loginDto){
        log.info("Login request received for email: {}", loginDto.email());

        UserResponseDto user = userService.authenticateUser(loginDto);

        ApiResponse<UserResponseDto> response = ApiResponse.success("Login successful", user);

        return ResponseEntity.ok(response);
    }

    /**
     * Get User by ID
     * GET /api/users/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponseDto>> getUserById(@PathVariable Long id){
        log.info("Get user request for ID: {}", id);

        UserResponseDto user = userService.getUserById(id);

        ApiResponse<UserResponseDto> response = ApiResponse.success("User retrieved successfully", user);

        return ResponseEntity.ok(response);
    }

    /**
     * Get User by email
     * GET /api/users/by-email?email=user@example.com
     */
    @GetMapping("/by-email")
    public ResponseEntity<ApiResponse<UserResponseDto>> getUserByEmail(@RequestParam("email") String email){
        log.info("Get user request for email: {}", email);

        UserResponseDto user = userService.getUserByEmail(email);

        ApiResponse<UserResponseDto> response = ApiResponse.success("User retrieved successfully", user);

        return ResponseEntity.ok(response);
    }

    /**
     * Update User profile
     * PUT /api/users/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateUser(
            @PathVariable long id,
            @Valid@RequestBody UserUpdateDto updateDto){

        log.info("Update user request for ID: {}", id);

        UserResponseDto user = userService.updateUser(id,updateDto);

        ApiResponse<UserResponseDto> response = ApiResponse.success("User updated successfully", user);

        return ResponseEntity.ok(response);
    }

    /**
     * Deactivate user account
     * DELETE /api/users/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deactivateUser(@PathVariable Long id) {

        log.info("Deactivate user request for ID: {}", id);

        userService.deactivateUser(id);

        ApiResponse<Void> response = ApiResponse.success("User deactivated successfully");

        return ResponseEntity.ok(response);
    }

    /**
     * Reactivate user account
     * POST /api/users/{id}/reactivate
     */
    @PostMapping("/{id}/reactivate")
    public ResponseEntity<ApiResponse<Void>> reactivateUser(@PathVariable Long id) {

        log.info("Reactivate user request for ID: {}", id);

        userService.reactivateUser(id);

        ApiResponse<Void> response = ApiResponse.success("User reactivated successfully");

        return ResponseEntity.ok(response);
    }

    // ================== SEARCH & LISTING ==================

    /**
     * Get all users with pagination
     * GET /api/users?page=0&size=10
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<UserResponseDto>>> getAllUsers(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size){

        log.info("Get all users requests - page: {}, size: {}", page,size);

        Pageable pageable = PageRequest.of(page,size);
        Page<UserResponseDto> users = userService.getAllUsers(pageable);

        ApiResponse<Page<UserResponseDto>> response = ApiResponse.success(
                "Users retireved successfully", users);

        return ResponseEntity.ok(response);
    }

    /**
     * Get users by role with pagination
     * GET /api/users/by-role/CUSTOMER?page=0&size=10
     */
    @GetMapping("/by-role/{role}")
    public ResponseEntity<ApiResponse<Page<UserResponseDto>>> getUsersByRole(
            @PathVariable("role") UserRole role,
            @RequestParam(value = "page",defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        log.info("Get users by role request - role: {}, page: {}, size: {}", role, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<UserResponseDto> users = userService.getUsersByRole(role, pageable);

        log.info("Found {} users for role: {}", users.getTotalElements(), role);

        ApiResponse<Page<UserResponseDto>> response = ApiResponse.success(
                "Users retrieved successfully", users);

        return ResponseEntity.ok(response);
    }


    /**
     * Search users by name
     * GET /api/users/search?name=john
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<UserResponseDto>>> searchUsers(
            @RequestParam("name") String name) {

        log.info("Search users request for name: {}", name);

        List<UserResponseDto> users = userService.searchUsersByName(name);

        ApiResponse<List<UserResponseDto>> response = ApiResponse.success(
                "Users retrieved successfully", users);

        return ResponseEntity.ok(response);
    }

    /**
     * Get staff members
     * GET /api/users/staff
     */
    @GetMapping("/staff")
    public ResponseEntity<ApiResponse<List<UserResponseDto>>> getStaffMembers() {

        log.info("Get staff members request");

        List<UserResponseDto> staff = userService.getStaffMembers();

        ApiResponse<List<UserResponseDto>> response = ApiResponse.success(
                "Staff members retrieved successfully", staff);

        return ResponseEntity.ok(response);
    }

    /**
     * Check if email exists
     * GET /api/users/exists?email=user@example.com
     */
    @GetMapping("/exists")
    public ResponseEntity<ApiResponse<Boolean>> checkEmailExists(
            @RequestParam("email") String email) {

        log.info("Check email exists request for: {}", email);

        boolean exists = userService.existsByEmail(email);

        ApiResponse<Boolean> response = ApiResponse.success(
                exists ? "Email already exists" : "Email is available", exists);

        return ResponseEntity.ok(response);
    }
}
