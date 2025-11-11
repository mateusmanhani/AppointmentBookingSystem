package com.barbershop.appointment_service.entity;

/**
 * Enum representing the lifecycle status of an appointment.
 */
public enum AppointmentStatus {
    /**
     * Customer has booked, waiting for shop owner confirmation
     */
    PENDING,
    
    /**
     * Shop owner has confirmed the appointment
     */
    CONFIRMED,
    
    /**
     * Appointment was cancelled by customer or shop owner
     */
    CANCELLED,
    
    /**
     * Service was completed successfully
     */
    COMPLETED,
    
    /**
     * Customer did not show up for the appointment
     */
    NO_SHOW
}
