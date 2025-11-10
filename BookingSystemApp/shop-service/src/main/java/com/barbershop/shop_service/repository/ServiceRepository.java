package com.barbershop.shop_service.repository;

import com.barbershop.shop_service.entity.ShopService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceRepository extends JpaRepository<ShopService, Long> {
    
    /**
     * Find all services for a specific shop.
     */
    List<ShopService> findByShopId(Long shopId);
    
    /**
     * Find a specific service by id and shopId.
     */
    Optional<ShopService> findByIdAndShopId(Long id, Long shopId);
}
