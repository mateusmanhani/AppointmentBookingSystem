package com.barbershop.appointment_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Entity representing a customer appointment at a shop.
 * 
 * This entity stores ONLY foreign key IDs to reference data in other services:
 * - customerId → user-service
 * - shopId → shop-service
 * - serviceId → shop-service
 * - employeeId → shop-service
 * 
 * Full details (names, prices, etc.) are fetched at runtime from respective services
 * to avoid data duplication and ensure current information.
 */
@Entity
@Table(name = "appointments", indexes = {
    @Index(name = "idx_customer_date", columnList = "customer_id, appointment_date"),
    @Index(name = "idx_shop_date", columnList = "shop_id, appointment_date, appointment_time"),
    @Index(name = "idx_employee_date", columnList = "employee_id, appointment_date"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_shop_status_date", columnList = "shop_id, status, appointment_date")
})
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ===== FOREIGN KEY REFERENCES (IDs only) =====
    
    /**
     * References User entity in user-service.
     * The customer who booked the appointment.
     */
    @NotNull
    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    /**
     * References Shop entity in shop-service.
     * The barbershop where the service will be performed.
     */
    @NotNull
    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    /**
     * References ShopService entity in shop-service.
     * The service that was booked (e.g., haircut, beard trim).
     */
    @NotNull
    @Column(name = "service_id", nullable = false)
    private Long serviceId;

    /**
     * References Employee entity in shop-service.
     * The specific employee requested by the customer.
     * Nullable - if null, any available employee can perform the service.
     */
    @Column(name = "employee_id")
    private Long employeeId;

    // ===== APPOINTMENT DETAILS =====

    /**
     * The date of the appointment.
     */
    @NotNull
    @Column(name = "appointment_date", nullable = false)
    private LocalDate appointmentDate;

    /**
     * The time of the appointment (24-hour format).
     */
    @NotNull
    @Column(name = "appointment_time", nullable = false)
    private LocalTime appointmentTime;

    /**
     * Current status of the appointment in its lifecycle.
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AppointmentStatus status = AppointmentStatus.PENDING;

    /**
     * Optional notes from the customer (special requests, first-time customer, etc.).
     */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // ===== AUDIT FIELDS =====

    /**
     * Timestamp when the appointment was created.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when the appointment was last updated.
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ===== CONSTRUCTORS =====

    public Appointment() {
    }

    public Appointment(Long customerId, Long shopId, Long serviceId, Long employeeId,
                      LocalDate appointmentDate, LocalTime appointmentTime, String notes) {
        this.customerId = customerId;
        this.shopId = shopId;
        this.serviceId = serviceId;
        this.employeeId = employeeId;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.notes = notes;
        this.status = AppointmentStatus.PENDING;
    }

    // ===== GETTERS AND SETTERS =====

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getShopId() {
        return shopId;
    }

    public void setShopId(Long shopId) {
        this.shopId = shopId;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(LocalDate appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public LocalTime getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(LocalTime appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // ===== UTILITY METHODS =====

    /**
     * Check if the appointment can be modified by the customer.
     * Only PENDING appointments can be changed.
     */
    public boolean canBeModified() {
        return this.status == AppointmentStatus.PENDING;
    }

    /**
     * Check if the appointment can be cancelled.
     * Appointments can be cancelled if they're PENDING or CONFIRMED.
     */
    public boolean canBeCancelled() {
        return this.status == AppointmentStatus.PENDING || 
               this.status == AppointmentStatus.CONFIRMED;
    }

    /**
     * Check if the appointment is in the past.
     */
    public boolean isPast() {
        LocalDateTime appointmentDateTime = LocalDateTime.of(appointmentDate, appointmentTime);
        return appointmentDateTime.isBefore(LocalDateTime.now());
    }

    /**
     * Check if the appointment is upcoming (in the future and not cancelled).
     */
    public boolean isUpcoming() {
        return !isPast() && 
               (status == AppointmentStatus.PENDING || status == AppointmentStatus.CONFIRMED);
    }

    @Override
    public String toString() {
        return "Appointment{" +
                "id=" + id +
                ", customerId=" + customerId +
                ", shopId=" + shopId +
                ", serviceId=" + serviceId +
                ", employeeId=" + employeeId +
                ", appointmentDate=" + appointmentDate +
                ", appointmentTime=" + appointmentTime +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }
}
