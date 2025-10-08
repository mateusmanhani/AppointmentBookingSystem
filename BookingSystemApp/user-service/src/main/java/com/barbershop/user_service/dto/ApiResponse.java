package com.barbershop.user_service.dto;

import java.time.LocalDateTime;

/**
 * Generic API response wrapper for consistent response format
 */
public record ApiResponse<T>(
        boolean success,
        String message,
        T data,
        LocalDateTime timeStamp
) {
    /**
     * Create success response
     */
    public static <T> ApiResponse<T> success(String message, T data){
        return new ApiResponse<>(true, message, data, LocalDateTime.now());
    }

    /**
     * Create success response without data
     */
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, message, null, LocalDateTime.now());
    }

    /**
     * Create error response
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null, LocalDateTime.now());
    }
}
