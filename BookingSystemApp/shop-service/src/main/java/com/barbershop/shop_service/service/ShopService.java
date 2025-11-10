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
        shop.setCity(dto.city());
        shop.setState(dto.state());
        shop.setZipCode(dto.zipCode());
        shop.setPhone(dto.phone());
        shop.setDescription(dto.description());
        shop.setOpeningTime(dto.openingTime());
        shop.setClosingTime(dto.closingTime());
        shop.setOwnerId(ownerId);
        if (dto.latitude() != null) shop.setLatitude(dto.latitude());
        else shop.setLatitude(0.0); // Default value
        if (dto.longitude() != null) shop.setLongitude(dto.longitude());
        else shop.setLongitude(0.0); // Default value
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

    /* Get shops by owner ID */
    public java.util.List<Shop> getShopsByOwnerId(Long ownerId) {
        log.info("Fetching shops for owner with id: {}", ownerId);
        return shopRepository.findByOwnerId(ownerId);
    }

    /* Update from DTO */
    public Shop updateFromDto(Long id, com.barbershop.shop_service.dto.ShopUpdateDto dto) {
        Shop shop = shopRepository.findById(id).orElseThrow();
        if (dto.name() != null) shop.setName(dto.name());
        if (dto.address() != null) shop.setAddress(dto.address());
        if (dto.city() != null) shop.setCity(dto.city());
        if (dto.state() != null) shop.setState(dto.state());
        if (dto.zipCode() != null) shop.setZipCode(dto.zipCode());
        if (dto.phone() != null) shop.setPhone(dto.phone());
        if (dto.description() != null) shop.setDescription(dto.description());
        if (dto.openingTime() != null) shop.setOpeningTime(dto.openingTime());
        if (dto.closingTime() != null) shop.setClosingTime(dto.closingTime());
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
