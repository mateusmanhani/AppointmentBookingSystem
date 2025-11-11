package com.barbershop.appointment_service.repository;

import com.barbershop.appointment_service.entity.Appointment;
import com.barbershop.appointment_service.entity.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for managing Appointment entities.
 * MVP version - only essential queries for customer and shop owner views.
 */
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // ===== CUSTOMER QUERIES (for customer dashboard) =====

    /**
     * Find all appointments for a customer.
     * Ordered by date descending (most recent first).
     * Used in: Customer dashboard to show "My Appointments"
     */
    List<Appointment> findByCustomerIdOrderByAppointmentDateDescAppointmentTimeDesc(Long customerId);

    // ===== SHOP QUERIES (for shop owner dashboard) =====

    /**
     * Find all appointments for a shop.
     * Ordered by date descending.
     * Used in: Shop owner dashboard to see all bookings
     */
    List<Appointment> findByShopIdOrderByAppointmentDateDescAppointmentTimeDesc(Long shopId);

    /**
     * Find appointments for a shop filtered by status.
     * Used in: Shop owner filtering by pending/confirmed/cancelled
     */
    List<Appointment> findByShopIdAndStatusOrderByAppointmentDateDescAppointmentTimeDesc(
        Long shopId, 
        AppointmentStatus status
    );

    // ===== AVAILABILITY QUERIES (for booking page) =====

    /**
     * Find active appointments (not cancelled) for a shop on a specific date.
     * Used to calculate available time slots.
     * Used in: Booking page showing available times
     */
    @Query("SELECT a FROM Appointment a WHERE a.shopId = :shopId " +
           "AND a.appointmentDate = :date " +
           "AND a.status IN ('PENDING', 'CONFIRMED') " +
           "ORDER BY a.appointmentTime ASC")
    List<Appointment> findActiveAppointmentsByShopAndDate(
        @Param("shopId") Long shopId,
        @Param("date") LocalDate date
    );
}
