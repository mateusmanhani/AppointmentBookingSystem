package com.barbershop.user_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Health check controller to verify database connectivity
 *  To be removed after main functionality is implemented
 */
@RestController
@RequestMapping("/health")
public class HealthController {
    @Autowired
    private DataSource dataSource;

    @GetMapping
    public Map<String, Object> health() {
        Map<String, Object> status = new HashMap<>();
        status.put("service", "user-service");
        status.put("status", "UP");
        status.put("timestamp", LocalDateTime.now());

        // Test Database connection
        try (Connection connection = dataSource.getConnection()) {
            status.put("database", "CONNECTED");
            status.put("database_url", connection.getMetaData().getURL());
            status.put("database_user", connection.getMetaData().getUserName());
        } catch (SQLException e){
            status.put("database", "DISCONNECTED");
            status.put("database_error", e.getMessage());
        }
        return status;
    }
}
