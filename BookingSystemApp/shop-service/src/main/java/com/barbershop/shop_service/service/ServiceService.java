package com.barbershop.shop_service.service;

import com.barbershop.shop_service.dto.ServiceRequestDto;
import com.barbershop.shop_service.dto.ServiceUpdateDto;
import com.barbershop.shop_service.entity.ShopService;
import com.barbershop.shop_service.repository.ServiceRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ServiceService {
    
    private final ServiceRepository serviceRepository;
    
    /**
     * Create a new service for a shop.
     */
    public ShopService createService(Long shopId, ServiceRequestDto dto) {
        log.info("Creating new service '{}' for shop id: {}", dto.name(), shopId);
        
        ShopService service = new ShopService();
        service.setShopId(shopId);
        service.setName(dto.name());
        service.setDescription(dto.description());
        service.setPrice(dto.price());
        service.setDuration(dto.duration());
        
        return serviceRepository.save(service);
    }
    
    /**
     * Get all services for a shop.
     */
    public List<ShopService> getServicesByShopId(Long shopId) {
        log.info("Fetching all services for shop id: {}", shopId);
        return serviceRepository.findByShopId(shopId);
    }
    
    /**
     * Get a service by its ID (without shop context).
     * Used by other microservices that only have serviceId.
     */
    public Optional<ShopService> getServiceById(Long serviceId) {
        log.info("Fetching service by id: {}", serviceId);
        return serviceRepository.findById(serviceId);
    }
    
    /**
     * Get a specific service by id and shopId.
     */
    public Optional<ShopService> getServiceByIdAndShopId(Long serviceId, Long shopId) {
        log.info("Fetching service id: {} for shop id: {}", serviceId, shopId);
        return serviceRepository.findByIdAndShopId(serviceId, shopId);
    }
    
    /**
     * Update an existing service.
     */
    public ShopService updateService(Long serviceId, Long shopId, ServiceUpdateDto dto) {
        log.info("Updating service id: {} for shop id: {}", serviceId, shopId);
        
        ShopService service = serviceRepository.findByIdAndShopId(serviceId, shopId)
            .orElseThrow(() -> new RuntimeException("Service not found"));
        
        if (dto.name() != null) {
            service.setName(dto.name());
        }
        if (dto.description() != null) {
            service.setDescription(dto.description());
        }
        if (dto.price() != null) {
            service.setPrice(dto.price());
        }
        if (dto.duration() != null) {
            service.setDuration(dto.duration());
        }
        
        return serviceRepository.save(service);
    }
    
    /**
     * Delete a service.
     */
    public boolean deleteService(Long serviceId, Long shopId) {
        log.info("Deleting service id: {} for shop id: {}", serviceId, shopId);
        
        Optional<ShopService> service = serviceRepository.findByIdAndShopId(serviceId, shopId);
        if (service.isEmpty()) {
            return false;
        }
        
        serviceRepository.delete(service.get());
        return true;
    }
}
