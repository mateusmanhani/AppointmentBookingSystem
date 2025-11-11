package com.barbershop.appointment_service.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO for creating a new appointment.
 * Customer provides: shop, service, date, time, and optional notes.
 * Employee can be selected later - for now, any available employee will be assigned.
 */
public record AppointmentRequestDto(
    
    @NotNull(message = "Shop ID is required")
    Long shopId,
    
    @NotNull(message = "Service ID is required")
    Long serviceId,
    
    @NotNull(message = "Appointment date is required")
    @Future(message = "Appointment date must be in the future")
    LocalDate appointmentDate,
    
    @NotNull(message = "Appointment time is required")
    LocalTime appointmentTime,
    
    String notes  // Optional - customer can add special requests
) {
    // customerId is extracted from JWT token (not in request body)
    // employeeId can be null for now (any available employee)
}
