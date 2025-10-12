package com.barbershop.user_service.service;

import com.barbershop.user_service.dto.*;
import com.barbershop.user_service.entity.User;
import com.barbershop.user_service.entity.UserRole;
import com.barbershop.user_service.exception.InvalidCredentialsException;
import com.barbershop.user_service.exception.UserAlreadyExistsException;
import com.barbershop.user_service.exception.UserNotFoundException;
import com.barbershop.user_service.mapper.UserMapper;
import com.barbershop.user_service.repository.UserRepository;
import com.barbershop.user_service.security.CustomUserDetailsService;
import com.barbershop.user_service.security.JwtService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@Slf4j
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CustomUserDetailsService customUserDetailsService;

    @Autowired
    public UserServiceImpl(UserRepository userRepository,
                           UserMapper userMapper,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService,
                           CustomUserDetailsService customUserDetailsService){
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Override
    @Transactional
    public UserResponseDto registerUser(UserRegistrationDto registrationDto) {
        log.info("Attempting to register user with email: {}", registrationDto.email());

        // Validate email is unique
        if (userRepository.existsByEmail(registrationDto.email().toLowerCase())){
            throw new UserAlreadyExistsException(
                    "User with email " + registrationDto.email() + " already exists."
            );
        }

        // Validate role-specific requirements
        validateRoleRequirements(registrationDto);

        // Encode password
        String encodedPassword = passwordEncoder.encode(registrationDto.password());

        // Map to entity and save
        User user = userMapper.toEntity(registrationDto,encodedPassword);
        User savedUser = userRepository.save(user);

        log.info("Successfully registered user with ID: {} and email {}",
                savedUser.getId(), savedUser.getEmail());

        return userMapper.toResponseDto(savedUser);
    }

    @Override
    @Transactional()
    public UserResponseDto authenticateUser(LoginRequestDto loginDto) {
        log.info("Attempting to authenticate user with email: {}", loginDto.email());

        // find active user by email
        Optional<User> userOptional = userRepository.findActiveUserByEmail(loginDto.email());

        if (userOptional.isEmpty()){
            log.warn("Authentication failed: User not found or inactive for email: {}", loginDto.email());
            throw  new InvalidCredentialsException("Invalid email or password");
        }

        User user = userOptional.get();

        //Check password
        if (!passwordEncoder.matches(loginDto.password(), user.getPassword())){
            log.warn("Authentication failed: Inavlid password for email: {}", loginDto.email());
            throw  new InvalidCredentialsException("Invalid email or password");
        }

        log.info("Successfully authenticated user with ID: {}", user.getId());

        return userMapper.toResponseDto(user);

    }


    @Override
    public JwtAuthResponseDto authenticateAndGenerateTokens(LoginRequestDto loginDto) {
        log.info("JWT authentication request for email: {}", loginDto.email());

        // find user in database
        Optional<User> userOptional = userRepository.findActiveUserByEmail(loginDto.email());

        if (userOptional.isEmpty()){
            log.warn("JWT authentication failed: User not found for email: {}", loginDto.email());
            throw new UserNotFoundException("Invalid email or password");
        }

        User user = userOptional.get();

        // check password
        if (!passwordEncoder.matches(loginDto.password(), user.getPassword())){
            log.warn("JWT authentication failed: Invalid password for email {}", loginDto.email() );
            throw new InvalidCredentialsException("Invalid email or password");
        }

        // Create UserDetails for JWT
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(user.getEmail());

        // Generate JWT tokens
        String accessToken = jwtService.generateToken(userDetails, user.getId(), user.getRole().name());
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        // Get expiration time and create response
        Long expiresIn = jwtService.getExpirationTime();
        UserResponseDto userResponse = userMapper.toResponseDto(user);

        log.info("Successfully generated JWT tokens for user ID: {}", user.getId());

        return JwtAuthResponseDto.create(accessToken,refreshToken,expiresIn,userResponse);
    }

    @Override
    public JwtAuthResponseDto refreshUserTokens(RefreshTokenRequestDto refreshRequest) {
        log.info("Processing token refresh request");

        try {
            // Extract username from refresh token
            String username = jwtService.extractUsername(refreshRequest.refreshToken());

            // Load user details
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

            // Validate refresh token
            if (jwtService.isTokenValid(refreshRequest.refreshToken(), userDetails)) {

                // Get user from database for response
                User user = userRepository.findActiveUserByEmail(username.toLowerCase())
                        .orElseThrow(() -> new InvalidCredentialsException("User not found"));

                // Generate new tokens
                String newAccessToken = jwtService.generateToken(userDetails, user.getId(), user.getRole().name());
                String newRefreshToken = jwtService.generateRefreshToken(userDetails);

                UserResponseDto userResponse = userMapper.toResponseDto(user);

                return JwtAuthResponseDto.create(
                        newAccessToken, newRefreshToken, jwtService.getExpirationTime(), userResponse);
            } else {
                throw new InvalidCredentialsException("Invalid refresh token");
            }
        } catch (Exception e) {
            log.error("Token refresh failed: {}", e.getMessage());
            throw new InvalidCredentialsException("Invalid refresh token");
        }
    }

    @Override
    @Transactional
    public UserResponseDto getUserById(Long id) {
        User user = findUserById(id);
        return userMapper.toResponseDto(user);
    }

    @Override
    @Transactional
    public UserResponseDto getUserByEmail(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email.toLowerCase());

        if (userOptional.isEmpty()){
            throw new UserNotFoundException("User not found with email: " + email);
        }

        return userMapper.toResponseDto(userOptional.get());
    }

    @Override
    public UserResponseDto updateUser(Long id, UserUpdateDto updateDto) {
        log.info("Updating user with ID: {}", id);

        User user = findUserById(id);

        // validate as least one field is provided
        if (!updateDto.hasValidFields()){
            throw new IllegalArgumentException("At least one field must be provided for update");
        }

        // Update fields
        userMapper.updateEntityFromDto(user,updateDto);
        User savedUser = userRepository.save(user);

        log.info("Successfully updated user with ID: {}", id);

        return userMapper.toResponseDto(savedUser);
    }

    @Override
    public void deactivateUser(Long id) {
        log.info("Deactivating user with ID: {}", id);

        User user = findUserById(id);
        user.setActive(false);
        userRepository.save(user);

        log.info("Successfully deactivated user with ID: {}", id);
    }

    @Override
    public void reactivateUser(Long id) {
        log.info("Reactivating user with ID: {}", id);

        User user = findUserById(id);
        user.setActive(true);
        userRepository.save(user);

        log.info("Successfully reactivated user with ID: {}", id);
    }

    @Override
    public Page<UserResponseDto> getAllUsers(Pageable pageable) {
        Page<User> users = userRepository.findAll(pageable); // Get page of user entities
        return users.map(userMapper::toResponseDto); //Transform Page<User> to Page<UserResponseDto>
    }

    @Override
    public Page<UserResponseDto> getUsersByRole(UserRole role, Pageable pageable) {
        Page<User> users = userRepository.findByRole(role,pageable);
        return users.map(userMapper::toResponseDto);
    }

    @Override
    @Transactional
    public List<UserResponseDto> searchUsersByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Search name cannot be empty");
        }

        List<User> users = userRepository.findByNameContainingIgnoreCase(name.trim());
        return userMapper.toResponseDtoList(users);
    }

    @Override
    @Transactional
    public List<UserResponseDto> getStaffMembers() {
        List<User> staffMembers = userRepository.findByRoleAndIsActiveTrue(UserRole.STAFF);
        return userMapper.toResponseDtoList(staffMembers);
    }

    @Override
    @Transactional
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email.toLowerCase());
    }

    // ================== PRIVATE HELPER METHODS ==================

    /**
     * Find user by ID or throw exception if not found
     */
    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));
    }

    /**
     * Validate role-specific requirements using Java 21 switch expression
     */
    private void validateRoleRequirements(UserRegistrationDto dto) {
        String missingFields = switch (dto.role()) {
            case CUSTOMER -> {
                if (dto.phone() == null || dto.phone().isBlank()) {
                    yield "Phone number is required for customers";
                }
                yield null;
            }
            case STAFF, SHOP_OWNER -> {
                StringBuilder missing = new StringBuilder();
                if (dto.phone() == null || dto.phone().isBlank()) {
                    missing.append("Phone number is required. ");
                }
                yield missing.length() > 0 ? missing.toString().trim() : null;
            }
        };

        if (missingFields != null) {
            throw new IllegalArgumentException(missingFields);
        }
    }
}
