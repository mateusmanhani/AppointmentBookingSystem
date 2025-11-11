package com.barbershop.appointment_service.client;

import com.barbershop.appointment_service.dto.UserDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Client for communicating with user-service.
 * 
 * BEGINNER EXPLANATION:
 * When showing appointments to shop owners, we need customer names and contact info.
 * Instead of storing customer data in our database, we ask user-service.
 * 
 * This keeps data in ONE place (user-service) = single source of truth!
 */
@Service
public class UserServiceClient {

    private static final Logger log = LoggerFactory.getLogger(UserServiceClient.class);
    
    private final RestTemplate restTemplate;
    private final String userServiceUrl;

    /**
     * Constructor - Spring automatically injects RestTemplate.
     */
    public UserServiceClient(
            RestTemplate restTemplate,
            @Value("${application.services.user-service.url}") String userServiceUrl) {
        this.restTemplate = restTemplate;
        this.userServiceUrl = userServiceUrl;
        log.info("UserServiceClient initialized with URL: {}", userServiceUrl);
    }

    /**
     * Get user/customer details by ID.
     * 
     * USAGE:
     * - Shop owner views appointment → needs customer name and contact
     * - Customer views own appointment → nice to show their info
     * 
     * @param userId The ID of the user/customer
     * @return UserDto with user details (name, email, phone)
     * @throws RuntimeException if user doesn't exist or service is down
     */
    @Cacheable(value = "users", key = "#userId")
    public UserDto getUser(Long userId) {
        try {
            String url = userServiceUrl + "/api/users/" + userId;
            log.debug("Fetching user {} from: {}", userId, url);
            
            UserDto user = restTemplate.getForObject(url, UserDto.class);
            
            if (user == null) {
                throw new RuntimeException("User not found: " + userId);
            }
            
            log.debug("Successfully fetched user: {}", user.email());
            return user;
            
        } catch (RestClientException e) {
            log.error("Failed to fetch user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Unable to fetch user details. User service may be unavailable.", e);
        }
    }
}
