package com.barbershop.appointment_service.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * DTO for creating a new appointment.
 * client sends a single ISO LocalDateTime for the appointment
 * (e.g. "2025-11-17T19:30:00"). Backend will split into date + time for storage.
 */
public record AppointmentRequestDto(

    @NotNull(message = "Shop ID is required")
    Long shopId,

    @NotNull(message = "Service ID is required")
    Long serviceId,

    @NotNull(message = "Appointment date/time is required")
    @Future(message = "Appointment date/time must be in the future")
    LocalDateTime appointmentDateTime,

    String notes  // Optional - customer can add special requests
) {
    // customerId is extracted from JWT token (not in request body)
    // employeeId can be null for now (any available employee)
}
