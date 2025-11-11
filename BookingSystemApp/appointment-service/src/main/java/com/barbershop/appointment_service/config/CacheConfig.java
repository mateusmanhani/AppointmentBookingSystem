package com.barbershop.appointment_service.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Configuration for caching external service data.
 * 
 * BEGINNER EXPLANATION:
 * Caching = Remembering information so you don't have to ask again.
 * 
 * Example:
 * - First time: "What's shop #5 info?" → Call shop-service (slow)
 * - Second time: "What's shop #5 info?" → Remember from cache (FAST!)
 * 
 * Cache expires after 10 minutes so data stays fresh.
 */
@Configuration
@EnableCaching  // This single annotation enables caching throughout the app!
public class CacheConfig {

    /**
     * Configure cache manager with 3 caches:
     * - "shops" - Stores shop information
     * - "services" - Stores service information  
     * - "employees" - Stores employee information
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
            "shops",      // Cache for shop data
            "services",   // Cache for service data
            "employees",  // Cache for employee data
            "users"       // Cache for user/customer data
        );
        
        // Configure how caching works
        cacheManager.setCaffeine(Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)  // Data expires after 10 minutes
            .maximumSize(1000)                        // Store max 1000 entries per cache
            .recordStats());                          // Track cache hits/misses (for monitoring)
        
        return cacheManager;
    }
}
