package com.barbershop.shop_service.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.barbershop.shop_service.entity.Shop;

@Repository
public interface ShopRepository extends JpaRepository<Shop, Long> {
    
    List<Shop> findByOwnerId(Long ownerId);

    Page<Shop> findAll(org.springframework.data.domain.Pageable pageable);

}
