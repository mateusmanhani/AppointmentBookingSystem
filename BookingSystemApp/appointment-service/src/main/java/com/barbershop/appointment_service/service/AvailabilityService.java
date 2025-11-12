package com.barbershop.appointment_service.service;

import com.barbershop.appointment_service.client.ShopServiceClient;
import com.barbershop.appointment_service.dto.ShopDto;
import com.barbershop.appointment_service.dto.TimeSlotDto;
import com.barbershop.appointment_service.entity.Appointment;
import com.barbershop.appointment_service.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for checking appointment availability.
 * 
 * BUSINESS LOGIC:
 * - Generate time slots based on shop opening hours
 * - Check which slots already have appointments
 * - Return list of available/unavailable slots for booking page
 * 
 * MVP APPROACH:
 * - Fixed 30-minute slots (09:00, 09:30, 10:00, etc.)
 * - One appointment per time slot (no overlaps)
 * - No employee availability checking yet
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AvailabilityService {

    private final AppointmentRepository appointmentRepository;
    private final ShopServiceClient shopServiceClient;

    // Constants for slot generation
    private static final int SLOT_DURATION_MINUTES = 30;  // Each slot is 30 minutes

    /**
     * Get available time slots for a shop on a specific date.
     * 
     * WORKFLOW:
     * 1. Fetch shop details to get opening/closing hours
     * 2. Generate all possible 30-minute time slots
     * 3. Find existing appointments for that day
     * 4. Mark slots as available or booked
     * 5. Return complete list for frontend
     * 
     * @param shopId The shop's ID
     * @param date The date to check availability
     * @return List of time slots with availability status
     */
    public List<TimeSlotDto> getAvailableSlots(Long shopId, LocalDate date) {
        log.info("Checking availability for shop {} on {}", shopId, date);
        
        // ===== STEP 1: Fetch shop details =====
        log.debug("Fetching shop details for shop ID: {}", shopId);
        ShopDto shop = shopServiceClient.getShop(shopId);
        
        if (shop == null) {
            log.error("Shop not found: {}", shopId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, 
                "Shop with ID " + shopId + " not found");
        }
        
        log.debug("Shop found: {} (hours: {} - {})", 
            shop.name(), shop.openingTime(), shop.closingTime());
        
        // ===== STEP 2: Parse shop opening hours =====
        LocalTime openingTime;
        LocalTime closingTime;
        
        try {
            // Shop hours are stored as strings like "09:00" and "18:00"
            openingTime = LocalTime.parse(shop.openingTime());
            closingTime = LocalTime.parse(shop.closingTime());
            
            log.debug("Parsed opening hours: {} - {}", openingTime, closingTime);
            
        } catch (DateTimeParseException | NullPointerException e) {
            log.error("Invalid shop hours format: opening={}, closing={}", 
                shop.openingTime(), shop.closingTime());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Shop has invalid opening hours configured");
        }
        
        // ===== STEP 3: Generate all possible time slots =====
        log.debug("Generating time slots from {} to {} with {}-minute intervals", 
            openingTime, closingTime, SLOT_DURATION_MINUTES);
        
        List<LocalTime> allTimeSlots = generateTimeSlots(openingTime, closingTime);
        log.debug("Generated {} time slots", allTimeSlots.size());
        
        // ===== STEP 4: Find existing appointments for this date =====
        log.debug("Checking for existing appointments on {}", date);
        
        List<Appointment> existingAppointments = appointmentRepository
            .findActiveAppointmentsByShopAndDate(shopId, date);
        
        log.debug("Found {} existing appointments", existingAppointments.size());
        
        // Create a Set of booked times for fast lookup
        // Set = no duplicates, O(1) lookup time vs O(n) for List
        Set<LocalTime> bookedTimes = existingAppointments.stream()
            .map(Appointment::getAppointmentTime)
            .collect(Collectors.toSet());
        
        log.debug("Booked times: {}", bookedTimes);
        
        // ===== STEP 5: Mark each slot as available or booked =====
        List<TimeSlotDto> slots = new ArrayList<>();
        
        for (LocalTime time : allTimeSlots) {
            if (bookedTimes.contains(time)) {
                // This time slot already has an appointment
                slots.add(TimeSlotDto.booked(time));
                log.trace("Slot {} - BOOKED", time);
            } else {
                // This time slot is free
                slots.add(TimeSlotDto.available(time));
                log.trace("Slot {} - AVAILABLE", time);
            }
        }
        
        log.info("Availability check complete: {} total slots, {} available, {} booked", 
            slots.size(), 
            slots.stream().filter(TimeSlotDto::available).count(),
            slots.stream().filter(s -> !s.available()).count());
        
        return slots;
    }

    /**
     * HELPER METHOD: Generate time slots between opening and closing time.
     * 
     * Example: 
     * - Opening: 09:00
     * - Closing: 18:00
     * - Duration: 30 minutes
     * 
     * Result: [09:00, 09:30, 10:00, 10:30, 11:00, 11:30, 12:00, 12:30, 
     *          13:00, 13:30, 14:00, 14:30, 15:00, 15:30, 16:00, 16:30, 17:00, 17:30]
     * 
     * Note: Last slot is 17:30 (not 18:00) because appointment would END at 18:00
     * 
     * @param openingTime Shop opening time
     * @param closingTime Shop closing time
     * @return List of all possible booking times
     */
    private List<LocalTime> generateTimeSlots(LocalTime openingTime, LocalTime closingTime) {
        List<LocalTime> slots = new ArrayList<>();
        
        LocalTime currentSlot = openingTime;
        
        // Generate slots until we reach closing time
        // Stop BEFORE closing time (last appointment starts 30 min before closing)
        while (currentSlot.isBefore(closingTime)) {
            slots.add(currentSlot);
            
            // Move to next slot (add 30 minutes)
            currentSlot = currentSlot.plusMinutes(SLOT_DURATION_MINUTES);
        }
        
        log.debug("Generated {} slots between {} and {}", 
            slots.size(), openingTime, closingTime);
        
        return slots;
    }
}
