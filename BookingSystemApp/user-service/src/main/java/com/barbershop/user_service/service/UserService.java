package com.barbershop.user_service.service;

import com.barbershop.user_service.dto.*;
import com.barbershop.user_service.entity.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service Interface for user management operations
 * defines the contract for user business logic
 */

public interface UserService {
    /**
     * Register a new user in the system
     */
    UserResponseDto registerUser(UserRegistrationDto registrationDto);

    /**
     * Authenticate User with email and password
     */
    UserResponseDto authenticateUser(LoginRequestDto loginRequestDto);

    /**
     * Authenticate User and generate JWT tokens
     */
    JwtAuthResponseDto authenticateAndGenerateTokens(LoginRequestDto loginDto);

    /**
     * Get user by ID
     */
    UserResponseDto getUserById(Long id);

    /**
     * Get user by email
     */
    UserResponseDto getUserByEmail(String email);

    /**
     * Update user profile information
     */
    UserResponseDto updateUser(Long id, UserUpdateDto updateDto);

    /**
     * Deactivate user account (soft delete)
     */
    void deactivateUser(Long id);

    /**
     * Reactivate user account
     */
    void reactivateUser(Long id);

    // ================== SEARCH & FILTERING ==================

    /**
     * Get all users with pagination
     */
    Page<UserResponseDto> getAllUsers(Pageable pageable);

    /**
     * Get users by role with pagination
     */
    Page<UserResponseDto> getUsersByRole(UserRole role, Pageable pageable);

    /**
     * Search users by name
     */
    List<UserResponseDto> searchUsersByName(String name);

    /**
     * Get staff members for shop operations
     */
    List<UserResponseDto> getStaffMembers();

    /**
     * Check if user exists by email
     */
    boolean existsByEmail(String email);
}

