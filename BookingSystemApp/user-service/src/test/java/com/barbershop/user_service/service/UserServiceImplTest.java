package com.barbershop.user_service.service;

import com.barbershop.user_service.dto.LoginRequestDto;
import com.barbershop.user_service.dto.UserRegistrationDto;
import com.barbershop.user_service.dto.UserResponseDto;
import com.barbershop.user_service.entity.User;
import com.barbershop.user_service.entity.UserRole;
import com.barbershop.user_service.exception.InvalidCredentialsException;
import com.barbershop.user_service.exception.UserAlreadyExistsException;
import com.barbershop.user_service.exception.UserNotFoundException;
import com.barbershop.user_service.mapper.UserMapper;
import com.barbershop.user_service.repository.UserRepository;
import com.barbershop.user_service.service.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserServiceImpl
 * Tests core business logic in isolation using mocks
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("User Service Unit Tests")
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private UserRegistrationDto validRegistrationDto;
    private User validUser;
    private UserResponseDto expectedResponse;
    private LoginRequestDto validLoginDto;

    @BeforeEach
    void setUp(){
        //  Test data setup
        validRegistrationDto = new UserRegistrationDto(
                "test@example.com",
                "Test123@Test",
                UserRole.CUSTOMER,
                "John",
                "Doe",
                "1234567890"
        );

        validUser = new User();
        validUser.setId(1L);
        validUser.setEmail("test@example.com");
        validUser.setRole(UserRole.CUSTOMER);
        validUser.setFirstName("John");
        validUser.setLastName("Doe");
        validUser.setPhone("1234567890");
        validUser.setActive(true);
        validUser.setCreatedAt(LocalDateTime.now());

        expectedResponse = new UserResponseDto(
                1L,
                "test@example.com",
                UserRole.CUSTOMER,
                "John",
                "Doe",
                "1234567890",
                true,    // isActive
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        validLoginDto = new LoginRequestDto("test@example.com", "Test123@Test");
    }

    @Test
    void shouldRegisterUser() {
        // 1. Create test data
        UserRegistrationDto registrationDto = new UserRegistrationDto(
                "test@example.com", "Test123@Test", UserRole.CUSTOMER,
                "John", "Doe", "1234567890"
        );

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setEmail("test@example.com");

        UserResponseDto expectedResponse = new UserResponseDto(
                1L, "test@example.com", UserRole.CUSTOMER, "John", "Doe",
                "1234567890", true, LocalDateTime.now(), LocalDateTime.now()
        );

        // 2. Tell mocks what to return
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Test123@Test")).thenReturn("encoded-password");
        when(userMapper.toEntity(any(), any())).thenReturn(savedUser);
        when(userRepository.save(any())).thenReturn(savedUser);
        when(userMapper.toResponseDto(any())).thenReturn(expectedResponse);

        // 3. Call the method we're testing
        UserResponseDto result = userService.registerUser(registrationDto);

        // 4. Check it worked
        assertNotNull(result);
        assertEquals("test@example.com", result.email());
        assertEquals("John", result.firstName());
    }

    @Test
    void shouldRejectDuplicateEmail() {
        // 1. Create test data
        UserRegistrationDto registrationDto = new UserRegistrationDto(
                "existing@example.com", "Test123@Test", UserRole.CUSTOMER,
                "John", "Doe", "1234567890"
        );

        // 2. Tell mock that email already exists
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // 3. Call the method and expect an exception
        assertThrows(Exception.class, () -> {
            userService.registerUser(registrationDto);
        });
    }

    @Test
    void shouldCheckEmailExists() {
        // 1. Tell mock what to return
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // 2. Call the method
        boolean exists = userService.existsByEmail("test@example.com");

        // 3. Check result
        assertTrue(exists);
    }
}
