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
    private static final int SLOT_DURATION_MINUTES = 15;  // Slot granularity: 15 minutes

    /**
     * Get available time slots for a shop on a specific date.
     * 
     * WORKFLOW:
     * 1. Fetch shop details to get opening/closing hours
     * 2. Generate all possible 15-minute time slots
     * 3. Find existing appointments for that day
     * 4. Mark slots as available or booked
     * 5. Return complete list for frontend
     * 
     * @param shopId The shop's ID
     * @param date The date to check availability
     * @return List of time slots with availability status
     */
    public List<TimeSlotDto> getAvailableSlots(Long shopId, LocalDate date, Long employeeId) {
        log.info("Checking availability for shop {} on {} (employeeId={})", shopId, date, employeeId);
        
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
        log.debug("Checking for existing appointments on {} (employee filter: {})", date, employeeId != null);

        List<Appointment> existingAppointments;
        if (employeeId != null) {
            existingAppointments = appointmentRepository
                .findActiveAppointmentsByShopAndDateAndEmployee(shopId, date, employeeId);
            log.debug("Found {} existing appointments for employee {}", existingAppointments.size(), employeeId);
        } else {
            existingAppointments = appointmentRepository
                .findActiveAppointmentsByShopAndDate(shopId, date);
            log.debug("Found {} existing appointments (all employees)", existingAppointments.size());
        }
        
        // Create time ranges for all booked appointments (start time -> end time)
        // Each appointment blocks time from startTime to (startTime + serviceDuration)
        log.debug("Building appointment time ranges...");
        
        List<AppointmentTimeRange> bookedRanges = existingAppointments.stream()
            .map(apt -> {
                LocalTime start = apt.getAppointmentTime();
                // Use stored service duration (snapshot pattern)
                Integer duration = apt.getServiceDuration();
                if (duration == null) {
                    log.warn("Appointment {} has null duration, defaulting to 30 minutes", apt.getId());
                    duration = 30;  // Fallback for old appointments without duration
                }
                LocalTime end = start.plusMinutes(duration);
                log.trace("Appointment {}: {} to {} ({} min)", apt.getId(), start, end, duration);
                return new AppointmentTimeRange(start, end);
            })
            .collect(Collectors.toList());
        
        log.debug("Created {} appointment time ranges", bookedRanges.size());
        
        // ===== STEP 5: Mark each slot as available or booked =====
        List<TimeSlotDto> slots = new ArrayList<>();
        
        for (LocalTime slotTime : allTimeSlots) {
            // Check if this 15-minute slot overlaps with ANY existing appointment
            boolean isOverlapping = bookedRanges.stream()
                .anyMatch(range -> slotOverlaps(slotTime, range));
            
            if (isOverlapping) {
                slots.add(TimeSlotDto.booked(slotTime));
                log.trace("Slot {} - BOOKED (overlaps with appointment)", slotTime);
            } else {
                slots.add(TimeSlotDto.available(slotTime));
                log.trace("Slot {} - AVAILABLE", slotTime);
            }
        }
        
        log.info("Availability check complete: {} total slots, {} available, {} booked (employeeId={})", 
            slots.size(), 
            slots.stream().filter(TimeSlotDto::available).count(),
            slots.stream().filter(s -> !s.available()).count(),
            employeeId);
        
        return slots;
    }
    
    /**
     * Check if a time slot overlaps with an appointment's time range.
     * A 15-minute slot is considered overlapping if it falls within the appointment duration.
     * 
     * Example:
     * - Appointment: 10:00 - 10:45 (45 minutes)
     * - Slot 10:00: OVERLAPS (starts within appointment)
     * - Slot 10:15: OVERLAPS (falls within appointment)
     * - Slot 10:30: OVERLAPS (falls within appointment)
     * - Slot 10:45: AVAILABLE (appointment ends, this slot is free)
     * 
     * Logic: slot overlaps if slot >= appointment.start AND slot < appointment.end
     * 
     * @param slotTime The time slot to check (e.g., 10:15)
     * @param range The appointment's time range (start to end)
     * @return true if the slot overlaps with the appointment
     */
    private boolean slotOverlaps(LocalTime slotTime, AppointmentTimeRange range) {
        // Slot is blocked if it starts within the appointment duration
        // Using !isBefore handles both "equals" and "after" cases for start
        // Using isBefore (not isAfter) means we DON'T block the exact end time
        return !slotTime.isBefore(range.start()) && slotTime.isBefore(range.end());
    }
    
    /**
     * Simple record to represent an appointment's time range.
     * Used for overlap detection when checking slot availability.
     */
    private record AppointmentTimeRange(LocalTime start, LocalTime end) {}

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
