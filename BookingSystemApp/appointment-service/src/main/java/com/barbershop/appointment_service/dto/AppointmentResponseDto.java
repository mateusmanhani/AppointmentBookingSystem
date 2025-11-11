package com.barbershop.appointment_service.dto;

import com.barbershop.appointment_service.entity.AppointmentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * DTO for returning appointment details to the frontend.
 * This is the "enriched" version with all related data fetched from other services.
 * 
 * For Customer Dashboard: Shows shop name, service name, date, time, status
 * For Shop Owner Dashboard: Shows customer name, contact info, service details
 */
public record AppointmentResponseDto(
    
    // Appointment basic info
    Long id,
    LocalDate appointmentDate,
    LocalTime appointmentTime,
    AppointmentStatus status,
    String notes,
    
    // Customer details (fetched from user-service)
    Long customerId,
    String customerName,      // firstName + lastName
    String customerEmail,
    String customerPhone,
    
    // Shop details (fetched from shop-service)
    Long shopId,
    String shopName,
    String shopAddress,
    String shopPhone,
    
    // Service details (fetched from shop-service)
    Long serviceId,
    String serviceName,
    BigDecimal servicePrice,  // Current price at display time
    Integer serviceDuration,  // in minutes
    
    // Employee details (fetched from shop-service, can be null)
    Long employeeId,
    String employeeName,
    
    // Audit
    LocalDateTime createdAt
) {
    
    /**
     * Get formatted date/time as string for display.
     * Example: "November 15, 2025 at 2:00 PM"
     */
    public String getFormattedDateTime() {
        return appointmentDate + " at " + appointmentTime;
    }
    
    /**
     * Check if appointment is upcoming (in the future).
     */
    public boolean isUpcoming() {
        LocalDateTime appointmentDateTime = LocalDateTime.of(appointmentDate, appointmentTime);
        return appointmentDateTime.isAfter(LocalDateTime.now());
    }
}
