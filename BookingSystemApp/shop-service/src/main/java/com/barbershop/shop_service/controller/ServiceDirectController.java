package com.barbershop.shop_service.controller;

import com.barbershop.shop_service.dto.ServiceResponseDto;
import com.barbershop.shop_service.entity.ShopService;
import com.barbershop.shop_service.service.ServiceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * REST controller for direct service access (without shop context).
 * Used primarily by other microservices to fetch service details.
 */
@RestController
@RequestMapping("/api/services")
public class ServiceDirectController {

    private final ServiceService serviceService;

    public ServiceDirectController(ServiceService serviceService) {
        this.serviceService = serviceService;
    }

    /**
     * Get a service by its ID directly.
     * GET /api/services/{id}
     * 
     * This endpoint allows fetching service details without knowing the shopId,
     * which is useful for the appointment-service when it only has serviceId.
     * 
     * @param id The service ID
     * @return Service details including duration
     */
    @GetMapping("/{id}")
    public ResponseEntity<ServiceResponseDto> getServiceById(@PathVariable Long id) {
        Optional<ShopService> serviceMaybe = serviceService.getServiceById(id);

        return serviceMaybe.map(s -> ResponseEntity.ok(new ServiceResponseDto(
                s.getId(), 
                s.getShopId(), 
                s.getName(), 
                s.getDescription(),
                s.getPrice(), 
                s.getDuration(), 
                s.getCreatedAt(), 
                s.getUpdatedAt())))
            .orElse(ResponseEntity.notFound().build());
    }
}
