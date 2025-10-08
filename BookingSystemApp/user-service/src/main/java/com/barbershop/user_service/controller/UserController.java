package com.barbershop.user_service.controller;

import com.barbershop.user_service.dto.ApiResponse;
import com.barbershop.user_service.dto.LoginRequestDto;
import com.barbershop.user_service.dto.UserRegistrationDto;
import com.barbershop.user_service.dto.UserResponseDto;
import com.barbershop.user_service.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


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

        ApiResponse<UserResponseDto> response = ApiResponse.success("Login successfull", user);

        return ResponseEntity.ok(response);
    }
}
