package com.barbershop.appointment_service.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * DTO for rescheduling an appointment.
 */
public record AppointmentRescheduleRequestDto(
    @NotNull @Future LocalDateTime newDateTime,
    Long employeeId,
    String notes
) {}
