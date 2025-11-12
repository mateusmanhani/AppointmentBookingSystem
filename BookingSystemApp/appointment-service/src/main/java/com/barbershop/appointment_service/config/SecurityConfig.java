package com.barbershop.appointment_service.config;

import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.crypto.SecretKey;
import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableMethodSecurity
@Slf4j
public class SecurityConfig {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Enable CORS
            .csrf(csrf -> csrf.disable()) // Disable CSRF for stateless JWT
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // No sessions
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/actuator/**").permitAll()
                
                // Availability endpoint - public (anyone can check available time slots)
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/availability/**").permitAll()
                
                // Appointment endpoints - require authentication
                // Customers: create appointments, view their own appointments
                // Shop owners: view appointments for their shops
                .requestMatchers("/api/appointments/**").authenticated()
                
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .decoder(jwtDecoder())
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
                .authenticationEntryPoint((request, response, authException) -> {
                    log.error("=== JWT Authentication Failed ===");
                    log.error("Error: {}", authException.getMessage());
                    log.error("Request URI: {}", request.getRequestURI());
                    log.error("Authorization header: {}", request.getHeader("Authorization"));
                    response.setStatus(401);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"" + authException.getMessage() + "\"}");
                })
            );

        return http.build();
    }
    
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        
        // Custom converter to extract role from JWT claims
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            // Get the role claim from JWT
            String role = jwt.getClaimAsString("role");
            
            log.info("JWT role claim: {}", role);
            
            if (role == null || role.isEmpty()) {
                log.warn("No role claim found in JWT");
                return Collections.emptyList();
            }
            
            // Add ROLE_ prefix if not already present
            if (!role.startsWith("ROLE_")) {
                role = "ROLE_" + role;
            }
            
            log.info("Granted authority: {}", role);
            return Collections.singletonList(new SimpleGrantedAuthority(role));
        });
        
        return converter;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Allow requests from frontend
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:8080", "http://127.0.0.1:8080"));
        
        // Allow all HTTP methods
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        
        // Allow all headers
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // Allow credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);
        
        // Expose Authorization header to frontend
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        
        // Cache preflight response for 1 hour
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        log.info("CORS configuration initialized: allowed origins = {}", configuration.getAllowedOrigins());
        
        return source;
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        log.info("Initializing JwtDecoder with secret key");
        
        // Use the SAME key generation method as user-service
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes());
        
        log.info("SecretKey algorithm: {}", key.getAlgorithm());
        
        // Configure decoder to use HS512 algorithm (must match user-service)
        return NimbusJwtDecoder.withSecretKey(key)
                .macAlgorithm(org.springframework.security.oauth2.jose.jws.MacAlgorithm.HS512)
                .build();
    }
}
