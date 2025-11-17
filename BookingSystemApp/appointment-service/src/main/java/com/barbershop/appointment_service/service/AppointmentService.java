package com.barbershop.appointment_service.service;

import com.barbershop.appointment_service.client.ShopServiceClient;
import com.barbershop.appointment_service.client.UserServiceClient;
import com.barbershop.appointment_service.dto.*;
import com.barbershop.appointment_service.entity.Appointment;
import com.barbershop.appointment_service.entity.AppointmentStatus;
import com.barbershop.appointment_service.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing appointments.
 * 
 * BUSINESS LOGIC:
 * - Validates that shop, service exist before creating appointment
 * - Ensures appointment date is in the future
 * - Enriches appointments with data from shop-service and user-service
 * - Handles appointment lifecycle (create, view, update status)
 */
@Service
@RequiredArgsConstructor  // Lombok generates constructor for final fields
@Slf4j  // Lombok generates logger
public class AppointmentService {

    // Dependencies injected via constructor
    private final AppointmentRepository appointmentRepository;
    private final ShopServiceClient shopServiceClient;
    private final UserServiceClient userServiceClient;

    /**
     * Create a new appointment for a customer.
     * Steps
     * 1. Validate shop exists (call shop-service)
     * 2. Validate service exists (call shop-service)
     * 3. Check date is in future
     * 4. Save appointment with PENDING status
     * 5. Return enriched response with full details
     * 
     * @param request The appointment request from customer
     * @param customerId The ID of the customer (extracted from JWT)
     * @return Enriched appointment with shop/service/customer details
     */
    @Transactional  // Database transaction - rollback if anything fails
    public AppointmentResponseDto createAppointment(AppointmentRequestDto request, Long customerId) {
        log.info("Creating appointment for customer {} at shop {}", customerId, request.shopId());
        
        // ===== STEP 1: Validate shop exists =====
        log.debug("Validating shop with ID: {}", request.shopId());
        ShopDto shop = shopServiceClient.getShop(request.shopId());
        if (shop == null) {
            log.error("Shop not found: {}", request.shopId());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, 
                "Shop with ID " + request.shopId() + " not found");
        }
        log.debug("Shop validated: {}", shop.name());
        
        // ===== STEP 2: Validate service exists =====
        log.debug("Validating service with ID: {}", request.serviceId());
    ServiceDto service = shopServiceClient.getService(request.shopId(), request.serviceId());
        if (service == null) {
            log.error("Service not found: {}", request.serviceId());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, 
                "Service with ID " + request.serviceId() + " not found");
        }
    log.debug("Service validated: {}", service.name());
        
        // ===== STEP 3: Validate appointmentDateTime is in the future =====
        // Additional check beyond @Future validation (in case it was skipped)
        LocalDateTime now = LocalDateTime.now();
        if (request.appointmentDateTime().isBefore(now)) {
            log.error("Appointment date/time {} is in the past", request.appointmentDateTime());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Appointment date/time must be in the future");
        }
        log.debug("Date/time validated: {}", request.appointmentDateTime());

        // ===== STEP 4: Build appointment entity (split LocalDateTime into date + time) =====
        Appointment appointment = new Appointment();
        appointment.setCustomerId(customerId);  // From JWT token
        appointment.setShopId(request.shopId());
        appointment.setServiceId(request.serviceId());
        // If employeeId provided, validate it belongs to the same shop and set it
        if (request.employeeId() != null) {
            try {
                var employee = shopServiceClient.getEmployee(request.shopId(), request.employeeId());
                if (employee == null) {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found: " + request.employeeId());
                }
                // Ownership already checked inside client; but double-check defensively
                if (employee.shopId() != null && !request.shopId().equals(employee.shopId())) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Employee does not belong to the selected shop");
                }
                appointment.setEmployeeId(request.employeeId());
            } catch (ResponseStatusException ex) {
                throw ex;
            } catch (Exception ex) {
                log.error("Error validating employee {}: {}", request.employeeId(), ex.getMessage());
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Unable to validate employee");
            }
        } else {
            appointment.setEmployeeId(null);
        }
        // Persist date and time separately (entity stores LocalDate + LocalTime)
        appointment.setAppointmentDate(request.appointmentDateTime().toLocalDate());
        // Truncate seconds/nanos for consistency with 30-min slot granularity
        LocalTime timeOnly = request.appointmentDateTime().toLocalTime().truncatedTo(ChronoUnit.MINUTES);
        appointment.setAppointmentTime(timeOnly);
        appointment.setStatus(AppointmentStatus.PENDING);  // New appointments start as PENDING
        appointment.setNotes(request.notes());
        
        // Save to database (createdAt/updatedAt set automatically by @CreationTimestamp)
        Appointment savedAppointment = appointmentRepository.save(appointment);
        log.info("Appointment created successfully with ID: {}", savedAppointment.getId());
        
        // ===== STEP 5: Enrich and return response =====
        // Fetch full details from other services and build complete DTO
        return enrichAppointment(savedAppointment);
    }

    /**
     * Get all appointments for a specific customer.
     * Returns enriched data with shop/service/employee names.
     * 
     * @param customerId The customer's user ID
     * @return List of appointments with full details
     */
    @Transactional(readOnly = true)  // Read-only transaction
    public List<AppointmentResponseDto> getCustomerAppointments(Long customerId) {
        log.info("Fetching appointments for customer: {}", customerId);
        
        List<Appointment> appointments = appointmentRepository
            .findByCustomerIdOrderByAppointmentDateDescAppointmentTimeDesc(customerId);
        
        log.debug("Found {} appointments for customer {}", appointments.size(), customerId);
        
        // Convert each Appointment entity to enriched DTO
        return appointments.stream()
            .map(this::enrichAppointment)  //call enrichAppointment for each
            .collect(Collectors.toList());
    }

    /**
     * Get all appointments for a specific shop.
     * Used by shop owners to see their bookings.
     * 
     * @param shopId The shop's ID
     * @return List of appointments with full details
     */
    @Transactional(readOnly = true)
    public List<AppointmentResponseDto> getShopAppointments(Long shopId) {
        log.info("Fetching appointments for shop: {}", shopId);
        
        List<Appointment> appointments = appointmentRepository
            .findByShopIdOrderByAppointmentDateDescAppointmentTimeDesc(shopId);
        
        log.debug("Found {} appointments for shop {}", appointments.size(), shopId);
        
        return appointments.stream()
            .map(this::enrichAppointment)
            .collect(Collectors.toList());
    }

    /**
     * HELPER METHOD: Enrich an appointment entity with data from other services.
     * 
     * - Calls user-service to get customer name/email/phone
     * - Calls shop-service to get shop details
     * - Calls shop-service to get service details
     * - Calls shop-service to get employee details (if assigned)
     * 
     * @param appointment The appointment entity (with just IDs)
     * @return Complete DTO with all related data
     */
    private AppointmentResponseDto enrichAppointment(Appointment appointment) {
        log.debug("Enriching appointment ID: {}", appointment.getId());
        
        // ===== Fetch customer details from user-service =====
        UserDto customer = userServiceClient.getUser(appointment.getCustomerId());
    log.debug("Customer fetched: {}", customer != null ? customer.email() : "null");
        
        // ===== Fetch shop details from shop-service =====
        ShopDto shop = shopServiceClient.getShop(appointment.getShopId());
    log.debug("Shop fetched: {}", shop != null ? shop.name() : "null");
        
        // ===== Fetch service details from shop-service =====
        ServiceDto service = shopServiceClient.getService(appointment.getShopId(), appointment.getServiceId());
    log.debug("Service fetched: {}", service != null ? service.name() : "null");
        
        // ===== Fetch employee details (if assigned) =====
        EmployeeDto employee = null;
        String employeeName = null;
        if (appointment.getEmployeeId() != null) {
            employee = shopServiceClient.getEmployee(appointment.getShopId(), appointment.getEmployeeId());
            employeeName = employee != null ? employee.name() : null;
            log.debug("Employee fetched: {}", employeeName);
        }
        
        // ===== Build complete response DTO =====
        // Handle null cases gracefully (shouldn't happen, but defensive programming)
        return new AppointmentResponseDto(
            // Appointment info
            appointment.getId(),
            appointment.getAppointmentDate(),
            appointment.getAppointmentTime(),
            appointment.getStatus(),
            appointment.getNotes(),
            
            // Customer details
            appointment.getCustomerId(),
            customer != null ? customer.getFullName() : "Unknown Customer",
            customer != null ? customer.email() : null,
            customer != null ? customer.phone() : null,
            
            // Shop details
            appointment.getShopId(),
            shop != null ? shop.name() : "Unknown Shop",
            shop != null ? shop.address() : null,
            shop != null ? shop.phone() : null,
            
            // Service details
            appointment.getServiceId(),
            service != null ? service.name() : "Unknown Service",
            service != null ? service.price() : null,
            service != null ? service.duration() : null,
            
            // Employee details (can be null for MVP)
            appointment.getEmployeeId(),
            employeeName,
            
            // Audit
            appointment.getCreatedAt()
        );
    }

    /**
     * Cancel an appointment owned by the given customer.
     * Rules:
     * - Appointment must exist and belong to the customer
     * - Appointment must be in the future
     * - Only pending or confirmed can be cancelled
     */
    @Transactional
    public AppointmentResponseDto cancelAppointment(Long appointmentId, Long customerId) {
        log.info("Customer {} requests cancellation of appointment {}", customerId, appointmentId);

        Appointment appt = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));

        if (!appt.getCustomerId().equals(customerId)) {
            log.warn("Customer {} attempted to cancel appointment {} they do not own", customerId, appointmentId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to cancel this appointment");
        }

        // Combine date and time for comparison
        LocalDateTime apptDateTime = LocalDateTime.of(appt.getAppointmentDate(), appt.getAppointmentTime());
        if (apptDateTime.isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot cancel past appointments");
        }

        if (appt.getStatus() == AppointmentStatus.CANCELLED) {
            // Idempotent: already cancelled
            log.info("Appointment {} already cancelled", appointmentId);
            return enrichAppointment(appt);
        }

        if (!(appt.getStatus() == AppointmentStatus.PENDING || appt.getStatus() == AppointmentStatus.CONFIRMED)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only pending or confirmed appointments can be cancelled");
        }

        appt.setStatus(AppointmentStatus.CANCELLED);
        Appointment saved = appointmentRepository.save(appt);
        log.info("Appointment {} cancelled by customer {}", appointmentId, customerId);
        return enrichAppointment(saved);
    }

    /**
     * Reschedule an existing appointment.
     */
    @Transactional
    public AppointmentResponseDto rescheduleAppointment(Long appointmentId, Long customerId, AppointmentRescheduleRequestDto request) {
        log.info("Customer {} requests reschedule of appointment {} to {} (employeeId={})",
            customerId, appointmentId, request.newDateTime(), request.employeeId());

        Appointment appt = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));

        if (!appt.getCustomerId().equals(customerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to modify this appointment");
        }

        // Validate new date/time
        if (request.newDateTime().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New appointment time must be in the future");
        }

        // Validate employee (if provided)
        Long newEmployeeId = request.employeeId();
        if (newEmployeeId != null) {
            try {
                var employee = shopServiceClient.getEmployee(appt.getShopId(), newEmployeeId);
                if (employee == null) {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found: " + newEmployeeId);
                }
                if (employee.shopId() != null && !appt.getShopId().equals(employee.shopId())) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Employee does not belong to the selected shop");
                }
            } catch (ResponseStatusException ex) {
                throw ex;
            } catch (Exception ex) {
                log.error("Error validating employee {}: {}", newEmployeeId, ex.getMessage());
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Unable to validate employee");
            }
        }

        // Check conflicts
        var newDate = request.newDateTime().toLocalDate();
        var newTime = request.newDateTime().toLocalTime().truncatedTo(ChronoUnit.MINUTES);
        boolean conflict;
        if (newEmployeeId != null) {
            conflict = appointmentRepository.existsActiveByShopDateTimeEmployeeExcludingId(
                appt.getShopId(), newDate, newTime, newEmployeeId, appt.getId());
        } else {
            conflict = appointmentRepository.existsActiveByShopAndDateTimeExcludingId(
                appt.getShopId(), newDate, newTime, appt.getId());
        }
        if (conflict) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Selected time slot is no longer available");
        }

        // Apply changes
        appt.setAppointmentDate(newDate);
        appt.setAppointmentTime(newTime);
        appt.setEmployeeId(newEmployeeId);
        if (request.notes() != null) {
            appt.setNotes(request.notes());
        }

        Appointment saved = appointmentRepository.save(appt);
        log.info("Appointment {} rescheduled successfully", appointmentId);
        return enrichAppointment(saved);
    }

    @Transactional(readOnly = true)
    public AppointmentResponseDto getAppointmentForCustomer(Long appointmentId, Long customerId) {
        Appointment appt = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));
        if (!appt.getCustomerId().equals(customerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to view this appointment");
        }
        return enrichAppointment(appt);
    }
}
