package com.barbershop.appointment_service.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.RestTemplate;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Configuration for RestTemplate - used to make HTTP calls to other services.
 *
 * This RestTemplate includes an interceptor that will forward the current
 * request's Bearer token (if present) so downstream services (shop/user)
 * can validate the caller.
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        RestTemplate restTemplate = builder
            .setConnectTimeout(Duration.ofSeconds(5))  // Wait max 5 seconds to connect
            .setReadTimeout(Duration.ofSeconds(5))     // Wait max 5 seconds for response
            .build();

        // Interceptor: add Authorization header when a Jwt is present in SecurityContext
        ClientHttpRequestInterceptor authInterceptor = (request, body, execution) -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null) {
                Object principal = auth.getPrincipal();
                String tokenValue = null;
                if (principal instanceof Jwt jwt) {
                    tokenValue = jwt.getTokenValue();
                } else if (auth.getCredentials() instanceof String) {
                    tokenValue = (String) auth.getCredentials();
                }
                if (tokenValue != null && !tokenValue.isBlank()) {
                    request.getHeaders().setBearerAuth(tokenValue);
                }
            }
            return execution.execute(request, body);
        };

        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>(restTemplate.getInterceptors());
        interceptors.add(authInterceptor);
        restTemplate.setInterceptors(interceptors);

        return restTemplate;
    }
}
