package com.barbershop.appointment_service.dto;

import java.time.LocalTime;

/**
 * DTO representing a single time slot for booking availability.
 * Used in the booking page to show which times are available/unavailable.
 */
public record TimeSlotDto(
    
    LocalTime time,           // e.g., "09:00", "09:30", "10:00"
    
    boolean available,        // true = customer can book this time
    
    String reason             // "available", "booked", or "closed"
) {
    
    /**
     * Create an available slot.
     */
    public static TimeSlotDto available(LocalTime time) {
        return new TimeSlotDto(time, true, "available");
    }
    
    /**
     * Create a booked (unavailable) slot.
     */
    public static TimeSlotDto booked(LocalTime time) {
        return new TimeSlotDto(time, false, "booked");
    }
    
    /**
     * Create a closed (outside opening hours) slot.
     */
    public static TimeSlotDto closed(LocalTime time) {
        return new TimeSlotDto(time, false, "closed");
    }
}
