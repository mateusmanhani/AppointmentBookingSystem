package com.barbershop.shop_service.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Test controller to debug JWT authentication
 */
@RestController
@RequestMapping("/api/test")
@Slf4j
public class TestController {

    @GetMapping("/auth")
    public Map<String, Object> testAuth(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        
        if (authentication == null) {
            response.put("error", "No authentication found");
            return response;
        }
        
        response.put("authenticated", authentication.isAuthenticated());
        response.put("principal", authentication.getPrincipal().getClass().getName());
        response.put("authorities", authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));
        
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            response.put("jwtClaims", jwt.getClaims());
            response.put("jwtSubject", jwt.getSubject());
            
            log.info("JWT Claims: {}", jwt.getClaims());
            log.info("JWT Subject: {}", jwt.getSubject());
            log.info("Authorities: {}", authentication.getAuthorities());
        }
        
        return response;
    }
}
