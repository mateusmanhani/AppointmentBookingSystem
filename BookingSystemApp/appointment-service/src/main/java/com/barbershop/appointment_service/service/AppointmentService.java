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
import java.time.format.DateTimeParseException;
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
        
        // ===== STEP 2: Validate service exists and fetch duration =====
        log.debug("Validating service with ID: {}", request.serviceId());
        ServiceDto service = shopServiceClient.getService(request.shopId(), request.serviceId());
        if (service == null) {
            log.error("Service not found: {}", request.serviceId());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, 
                "Service with ID " + request.serviceId() + " not found");
        }
        log.debug("Service validated: {} (duration: {} min)", service.name(), service.duration());
        
        // ===== STEP 3: Validate service duration is available =====
        if (service.duration() == null || service.duration() <= 0) {
            log.error("Service {} has invalid duration: {}", service.id(), service.duration());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                "Service has invalid duration configured");
        }
        
        // ===== STEP 4: Validate appointmentDateTime is in the future =====
        // Additional check beyond @Future validation (in case it was skipped)
        LocalDateTime now = LocalDateTime.now();
        if (request.appointmentDateTime().isBefore(now)) {
            log.error("Appointment date/time {} is in the past", request.appointmentDateTime());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Appointment date/time must be in the future");
        }
        log.debug("Date/time validated: {}", request.appointmentDateTime());
        
        // ===== STEP 5: Validate appointment doesn't extend past closing time =====
        // Parse shop closing time
        LocalTime closingTime;
        try {
            closingTime = LocalTime.parse(shop.closingTime());
        } catch (DateTimeParseException e) {
            log.error("Shop {} has invalid closing time: {}", shop.id(), shop.closingTime());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                "Shop has invalid closing time configured");
        }
        
        // Calculate appointment end time (start time + service duration)
        LocalTime appointmentTime = request.appointmentDateTime().toLocalTime().truncatedTo(ChronoUnit.MINUTES);
        LocalTime appointmentEndTime = appointmentTime.plusMinutes(service.duration());
        
        // Appointment must end before or at closing time
        if (appointmentEndTime.isAfter(closingTime)) {
            log.error("Appointment for service {} ({} min) starting at {} would end at {} (past closing time {})",
                service.name(), service.duration(), appointmentTime, appointmentEndTime, closingTime);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Selected time slot is too late. This service would extend past shop closing time (" + closingTime + ")");
        }
        log.debug("Appointment end time {} validated (within closing time {})", appointmentEndTime, closingTime);

        // ===== STEP 6: Build appointment entity (split LocalDateTime into date + time) =====
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
        // Truncate seconds/nanos for consistency with 15-min slot granularity
        LocalTime timeOnly = request.appointmentDateTime().toLocalTime().truncatedTo(ChronoUnit.MINUTES);
        appointment.setAppointmentTime(timeOnly);
        appointment.setStatus(AppointmentStatus.PENDING);  // New appointments start as PENDING
        appointment.setNotes(request.notes());
        
        // Store service duration for availability calculations (snapshot pattern)
        appointment.setServiceDuration(service.duration());
        log.debug("Stored service duration: {} minutes", service.duration());
        
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
        UserDto customer = null;
        try {
            customer = userServiceClient.getUser(appointment.getCustomerId());
            log.debug("Customer fetched: {}", customer != null ? customer.email() : "null");
        } catch (Exception e) {
            log.error("Failed to fetch customer {} for appointment {}: {}", 
                appointment.getCustomerId(), appointment.getId(), e.getMessage());
        }
        
        // ===== Fetch shop details from shop-service =====
        ShopDto shop = null;
        try {
            shop = shopServiceClient.getShop(appointment.getShopId());
            log.debug("Shop fetched: {}", shop != null ? shop.name() : "null");
        } catch (Exception e) {
            log.error("Failed to fetch shop {} for appointment {}: {}", 
                appointment.getShopId(), appointment.getId(), e.getMessage());
        }
        
        // ===== Fetch service details from shop-service =====
        ServiceDto service = null;
        try {
            service = shopServiceClient.getService(appointment.getShopId(), appointment.getServiceId());
            log.debug("Service fetched: {}", service != null ? service.name() : "null");
        } catch (Exception e) {
            log.error("Failed to fetch service {} for shop {} for appointment {}: {}", 
                appointment.getServiceId(), appointment.getShopId(), appointment.getId(), e.getMessage());
        }
        
        // ===== Fetch employee details (if assigned) =====
        EmployeeDto employee = null;
        String employeeName = null;
        if (appointment.getEmployeeId() != null) {
            try {
                employee = shopServiceClient.getEmployee(appointment.getShopId(), appointment.getEmployeeId());
                employeeName = employee != null ? employee.name() : null;
                log.debug("Employee fetched: {}", employeeName);
            } catch (Exception e) {
                log.error("Failed to fetch employee {} for shop {} for appointment {}: {}", 
                    appointment.getEmployeeId(), appointment.getShopId(), appointment.getId(), e.getMessage());
            }
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
