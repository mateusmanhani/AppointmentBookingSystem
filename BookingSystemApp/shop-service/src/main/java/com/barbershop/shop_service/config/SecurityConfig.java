package com.barbershop.shop_service.config;

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

import javax.crypto.SecretKey;
import java.util.Collections;

@Configuration
@EnableMethodSecurity
@Slf4j
public class SecurityConfig {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Disable CSRF for stateless JWT
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // No sessions
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/actuator/**").permitAll()
                .requestMatchers("/api/test/**").permitAll() // Test endpoint
                
                // Shop endpoints - GET is public, POST/PUT/DELETE require authentication
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/shops", "/api/shops/**").permitAll()
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/shops").authenticated()
                .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/shops/**").authenticated()
                .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/shops/**").authenticated()
                
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
                    log.error("Exception type: {}", authException.getClass().getName());
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
            log.info("JWT all claims: {}", jwt.getClaims());
            
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
    public JwtDecoder jwtDecoder() {
        log.info("Initializing JwtDecoder with secret key length: {}", secretKey.length());
        
        // Use the SAME key generation method as user-service
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes());
        
        log.info("SecretKey algorithm: {}", key.getAlgorithm());
        
        // Configure decoder to use HS512 algorithm (must match user-service)
        return NimbusJwtDecoder.withSecretKey(key)
                .macAlgorithm(org.springframework.security.oauth2.jose.jws.MacAlgorithm.HS512)
                .build();
    }
}
