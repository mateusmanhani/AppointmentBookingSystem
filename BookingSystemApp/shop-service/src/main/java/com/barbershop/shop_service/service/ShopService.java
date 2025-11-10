package com.barbershop.shop_service.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.barbershop.shop_service.repository.ShopRepository;
import com.barbershop.shop_service.entity.Shop;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ShopService {
    
    private final ShopRepository shopRepository;

    /* Create new shop */
    public Shop createShop(Shop shop) {
        log.info("Creating new shop: {}", shop.getName());
        return shopRepository.save(shop);
    }

    /* Create from DTO */
    public Shop createFromDto(com.barbershop.shop_service.dto.ShopRequestDto dto, Long ownerId) {
        Shop shop = new Shop();
        shop.setName(dto.name());
        shop.setAddress(dto.address());
        shop.setOwnerId(ownerId);
        if (dto.latitude() != null) shop.setLatitude(dto.latitude());
        if (dto.longitude() != null) shop.setLongitude(dto.longitude());
        return shopRepository.save(shop);
    }

    /* Get shop by ID */
    public Optional<Shop> getShopById(Long id) {
        log.info("Fetching shop with id: {}", id);
        return shopRepository.findById(id);
    }

    /* Get all shops */
    public Page<Shop> findAll(Pageable pageable) {
        log.info("Fetching all shops with pagination: {}", pageable);
        return shopRepository.findAll(pageable);
    }

    /* Update from DTO */
    public Shop updateFromDto(Long id, com.barbershop.shop_service.dto.ShopUpdateDto dto) {
        Shop shop = shopRepository.findById(id).orElseThrow();
        if (dto.name() != null) shop.setName(dto.name());
        if (dto.address() != null) shop.setAddress(dto.address());
        if (dto.latitude() != null) shop.setLatitude(dto.latitude());
        if (dto.longitude() != null) shop.setLongitude(dto.longitude());
        return shopRepository.save(shop);
    }

    /* Delete by id */
    public boolean deleteById(Long id) {
        if (!shopRepository.existsById(id)) return false;
        shopRepository.deleteById(id);
        return true;
    }
}
