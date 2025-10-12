package com.barbershop.user_service.service;

import com.barbershop.user_service.controller.UserController;
import com.barbershop.user_service.dto.UserRegistrationDto;
import com.barbershop.user_service.dto.UserResponseDto;
import com.barbershop.user_service.entity.UserRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
/**
 * Simple controller tests for beginners
 * Tests if the API endpoints work correctly
 */
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldRegisterUser() throws Exception {
        // 1. Create test data
        UserRegistrationDto request = new UserRegistrationDto(
                "test@example.com", "Test123@Test", UserRole.CUSTOMER,
                "John", "Doe", "1234567890"
        );

        UserResponseDto response = new UserResponseDto(
                1L, "test@example.com", UserRole.CUSTOMER, "John", "Doe",
                "1234567890", true, LocalDateTime.now(), LocalDateTime.now()
        );

        // 2. Tell service what to return
        when(userService.registerUser(any())).thenReturn(response);

        // 3. Call the API endpoint
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                // 4. Check the response
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("test@example.com"));
    }

    @Test
    void shouldGetUserById() throws Exception {
        // 1. Create test data
        UserResponseDto response = new UserResponseDto(
                1L, "test@example.com", UserRole.CUSTOMER, "John", "Doe",
                "1234567890", true, LocalDateTime.now(), LocalDateTime.now()
        );

        // 2. Tell service what to return
        when(userService.getUserById(1L)).thenReturn(response);

        // 3. Call the API and check response
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.email").value("test@example.com"));
    }
}
