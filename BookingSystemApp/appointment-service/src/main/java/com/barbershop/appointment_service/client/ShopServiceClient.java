package com.barbershop.appointment_service.client;

import com.barbershop.appointment_service.dto.EmployeeDto;
import com.barbershop.appointment_service.dto.ServiceDto;
import com.barbershop.appointment_service.dto.ShopDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Client for communicating with shop-service.
 * 
 * BEGINNER EXPLANATION:
 * This class is like a "messenger" that asks shop-service for information.
 * Instead of storing shop names, service prices, etc. in our database,
 * we ask shop-service whenever we need that info.
 * 
 * Caching makes it fast: First call is slow, next calls are instant!
 */
@Service
public class ShopServiceClient {

    private static final Logger log = LoggerFactory.getLogger(ShopServiceClient.class);
    
    private final RestTemplate restTemplate;
    private final String shopServiceUrl;

    /**
     * Constructor - Spring automatically injects RestTemplate.
     * @Value reads the URL from application.yml
     */
    public ShopServiceClient(
            RestTemplate restTemplate,
            @Value("${application.services.shop-service.url}") String shopServiceUrl) {
        this.restTemplate = restTemplate;
        this.shopServiceUrl = shopServiceUrl;
        log.info("ShopServiceClient initialized with URL: {}", shopServiceUrl);
    }

    /**
     * Get shop details by ID.
     * 
     * HOW IT WORKS:
     * 1. First call: Makes HTTP GET to shop-service → stores result in cache
     * 2. Next calls: Returns from cache (no HTTP call!) → FAST!
     * 
     * @Cacheable("shops") means: "Save the result with key = shopId"
     * 
     * @param shopId The ID of the shop to fetch
     * @return ShopDto with shop details (name, address, hours, etc.)
     * @throws RuntimeException if shop doesn't exist or service is down
     */
    @Cacheable(value = "shops", key = "#shopId")
    public ShopDto getShop(Long shopId) {
        try {
            String url = shopServiceUrl + "/api/shops/" + shopId;
            log.debug("Fetching shop {} from: {}", shopId, url);
            
            // Make HTTP GET request - RestTemplate converts JSON to ShopDto automatically!
            ShopDto shop = restTemplate.getForObject(url, ShopDto.class);
            
            if (shop == null) {
                throw new RuntimeException("Shop not found: " + shopId);
            }
            
            log.debug("Successfully fetched shop: {}", shop.name());
            return shop;
            
        } catch (RestClientException e) {
            log.error("Failed to fetch shop {}: {}", shopId, e.getMessage());
            throw new RuntimeException("Unable to fetch shop details. Shop service may be unavailable.", e);
        }
    }

    /**
     * Get service details by ID.
     * 
     * EXAMPLE:
     * serviceId = 10 → Returns: {id: 10, name: "Haircut", price: 25.00, duration: 30}
     * 
     * @param serviceId The ID of the service to fetch
     * @return ServiceDto with service details (name, price, duration)
     * @throws RuntimeException if service doesn't exist or service is down
     */
    @Cacheable(value = "services", key = "#serviceId")
    public ServiceDto getService(Long serviceId) {
        try {
            String url = shopServiceUrl + "/api/services/" + serviceId;
            log.debug("Fetching service {} from: {}", serviceId, url);
            
            ServiceDto service = restTemplate.getForObject(url, ServiceDto.class);
            
            if (service == null) {
                throw new RuntimeException("Service not found: " + serviceId);
            }
            
            log.debug("Successfully fetched service: {}", service.name());
            return service;
            
        } catch (RestClientException e) {
            log.error("Failed to fetch service {}: {}", serviceId, e.getMessage());
            throw new RuntimeException("Unable to fetch service details. Shop service may be unavailable.", e);
        }
    }

    /**
     * Get employee details by ID.
     * 
     * NULLABLE: Employee can be null (customer selected "any available")
     * 
     * @param employeeId The ID of the employee to fetch (can be null)
     * @return EmployeeDto with employee details (name, role), or null if employeeId is null
     * @throws RuntimeException if employee doesn't exist or service is down
     */
    @Cacheable(value = "employees", key = "#employeeId", unless = "#employeeId == null")
    public EmployeeDto getEmployee(Long employeeId) {
        if (employeeId == null) {
            log.debug("Employee ID is null, returning null (any available employee)");
            return null;
        }
        
        try {
            String url = shopServiceUrl + "/api/employees/" + employeeId;
            log.debug("Fetching employee {} from: {}", employeeId, url);
            
            EmployeeDto employee = restTemplate.getForObject(url, EmployeeDto.class);
            
            if (employee == null) {
                throw new RuntimeException("Employee not found: " + employeeId);
            }
            
            log.debug("Successfully fetched employee: {}", employee.name());
            return employee;
            
        } catch (RestClientException e) {
            log.error("Failed to fetch employee {}: {}", employeeId, e.getMessage());
            throw new RuntimeException("Unable to fetch employee details. Shop service may be unavailable.", e);
        }
    }

    /**
     * DEBUGGING TIP:
     * If you see log messages like "Fetching shop 5 from...", it's NOT using cache.
     * If you DON'T see these messages, cache is working! ✅
     * 
     * To test caching:
     * 1. Call getShop(5) → See log message (cache MISS)
     * 2. Call getShop(5) again → No log message (cache HIT!)
     */
}
