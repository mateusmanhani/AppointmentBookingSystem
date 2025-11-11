package com.barbershop.appointment_service.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Configuration for RestTemplate - used to make HTTP calls to other services.
 * 
 * BEGINNER EXPLANATION:
 * RestTemplate is like a phone that lets this service call other services.
 * Think of it as: "Hey shop-service, can you tell me about shop #5?"
 */
@Configuration
public class RestTemplateConfig {

    /**
     * Create a RestTemplate bean that Spring can inject anywhere.
     * 
     * @Bean means: "Spring, create this object and keep it ready to use"
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
            .setConnectTimeout(Duration.ofSeconds(5))  // Wait max 5 seconds to connect
            .setReadTimeout(Duration.ofSeconds(5))     // Wait max 5 seconds for response
            .build();
    }
}
