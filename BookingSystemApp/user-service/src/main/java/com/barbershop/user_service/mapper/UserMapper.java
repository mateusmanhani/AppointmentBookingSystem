package com.barbershop.user_service.mapper;

import com.barbershop.user_service.dto.*;
import com.barbershop.user_service.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Manual mapper for User entity and DTOs using Java 21 features
 * Provides clean separation between data layers
 */
@Component
public class UserMapper {
    /**
     * Convert UserRegistrationDto to User entity
     * Password encoding handled in service layer
     */
    public User toEntity(UserRegistrationDto dto, String encodedPassword){
        User user = new User();
        user.setEmail(dto.email().toLowerCase().trim());
        user.setPassword(encodedPassword);
        user.setRole(dto.role());
        user.setFirstName(dto.firstName().trim());
        user.setLastName(dto.lastName().trim());
        user.setPhone(dto.phone() != null ? dto.phone().trim() : null);
        user.setActive(true);

        return user;
    }

    /**
     * Convert User entity to UserResponseDto (excludes password)
     */
    public UserResponseDto toResponseDto (User user){
        return new UserResponseDto(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                user.getActive(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    /**
     * Convert list of User entities to UserResponseDto list
     */
    public List<UserResponseDto> toResponseDtoList(List<User> users){
        return users.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Update User entity from UserUpdateDto (partial updates)
     */
    public void updateEntityFromDto (User user, UserUpdateDto updateDto){
        if (updateDto.firstName() != null && !updateDto.firstName().isBlank()) {
            user.setFirstName(updateDto.firstName().trim());
        }

        if (updateDto.lastName() != null && !updateDto.lastName().isBlank()) {
            user.setLastName(updateDto.lastName().trim());
        }

        if (updateDto.phone() != null && !updateDto.phone().isBlank()) {
            user.setPhone(updateDto.phone().trim());
        }
    }
}
