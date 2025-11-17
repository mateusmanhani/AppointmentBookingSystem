package com.barbershop.appointment_service.controller;

import com.barbershop.appointment_service.dto.AppointmentRequestDto;
import com.barbershop.appointment_service.dto.AppointmentResponseDto;
import com.barbershop.appointment_service.dto.AppointmentRescheduleRequestDto;
import com.barbershop.appointment_service.dto.TimeSlotDto;
import com.barbershop.appointment_service.service.AppointmentService;
import com.barbershop.appointment_service.service.AvailabilityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST Controller for appointment management.
 * 
 * ENDPOINTS:
 * - POST /api/appointments - Create new appointment (CUSTOMER only)
 * - GET /api/appointments/my-appointments - View customer's appointments (CUSTOMER only)
 * - GET /api/availability/shop/{shopId}/date/{date} - Check available time slots (PUBLIC)
 * 
 * SECURITY:
 * - JWT required for appointment operations
 * - Public access for availability checking
 * - Role-based access control with @PreAuthorize
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final AvailabilityService availabilityService;

    /**
     * CREATE APPOINTMENT
     * 
     * Endpoint: POST /api/appointments
     * Access: CUSTOMER role only
     * 
     * WORKFLOW:
     * 1. Extract customer ID from JWT token
     * 2. Validate request body (@Valid annotation)
     * 3. Call AppointmentService to create appointment
     * 4. Return 201 Created with appointment details
     * 
    * EXAMPLE REQUEST :
    * POST /api/appointments
    * Authorization: Bearer <JWT_TOKEN>
    * Content-Type: application/json
    * 
    * {
    *   "shopId": 5,
    *   "serviceId": 10,
    *   "employeeId": 42, // optional
    *   "appointmentDateTime": "2025-11-15T14:00:00",
    *   "notes": "Please use organic products"
    * }
     * 
     * EXAMPLE RESPONSE (201 Created):
     * {
     *   "id": 123,
     *   "shopName": "Downtown Barbershop",
     *   "serviceName": "Haircut",
     *   "appointmentDate": "2025-11-15",
     *   "appointmentTime": "14:00",
     *   "status": "PENDING",
     *   ...
     * }
     */
    @PostMapping("/appointments")
    @PreAuthorize("hasRole('CUSTOMER')")  // Only customers can create appointments
    public ResponseEntity<AppointmentResponseDto> createAppointment(
        @Valid @RequestBody AppointmentRequestDto request,
        @AuthenticationPrincipal Jwt jwt  // Spring extracts JWT from Authorization header
    ) {
        // Extract customer ID from JWT token
        // The JWT was created by user-service and contains userId claim
        Long customerId = jwt.getClaim("userId");
        
        log.info("Customer {} creating appointment at shop {} for service {}", 
            customerId, request.shopId(), request.serviceId());
        
        // Call service to create appointment (validation + save + enrich)
        AppointmentResponseDto appointment = appointmentService.createAppointment(request, customerId);
        
        log.info("Appointment {} created successfully for customer {}", 
            appointment.id(), customerId);
        
        // Return 201 Created with the appointment in response body
        return ResponseEntity
            .status(HttpStatus.CREATED)  // 201 status code
            .body(appointment);
    }

    /**
     * VIEW MY APPOINTMENTS
     * 
     * Endpoint: GET /api/appointments/my-appointments
     * Access: CUSTOMER role only
     * 
     * WORKFLOW:
     * 1. Extract customer ID from JWT token
     * 2. Fetch all appointments for this customer
     * 3. Return list sorted by date (newest first)
     * 
     * SECURITY:
     * - Customer can ONLY see their own appointments
     * - Customer ID comes from JWT (can't be faked)
     * 
     * EXAMPLE REQUEST:
     * GET /api/appointments/my-appointments
     * Authorization: Bearer <JWT_TOKEN>
     * 
     * EXAMPLE RESPONSE (200 OK):
     * [
     *   {
     *     "id": 123,
     *     "shopName": "Downtown Barbershop",
     *     "serviceName": "Haircut",
     *     "appointmentDate": "2025-11-15",
     *     "appointmentTime": "14:00",
     *     "status": "PENDING"
     *   },
     *   {
     *     "id": 122,
     *     "shopName": "Uptown Salon",
     *     "serviceName": "Hair Coloring",
     *     "appointmentDate": "2025-11-10",
     *     "appointmentTime": "10:00",
     *     "status": "COMPLETED"
     *   }
     * ]
     */
    @GetMapping("/appointments/my-appointments")
    @PreAuthorize("hasRole('CUSTOMER')")  // Only customers can view their appointments
    public ResponseEntity<List<AppointmentResponseDto>> getMyAppointments(
        @AuthenticationPrincipal Jwt jwt
    ) {
        // Extract customer ID from JWT
        Long customerId = jwt.getClaim("userId");
        
        log.info("Customer {} fetching their appointments", customerId);
        
        // Fetch all appointments for this customer
        List<AppointmentResponseDto> appointments = appointmentService.getCustomerAppointments(customerId);
        
        log.info("Found {} appointments for customer {}", appointments.size(), customerId);
        
        return ResponseEntity.ok(appointments);  // 200 OK
    }

    /**
     * CHECK AVAILABILITY
     * 
     * Endpoint: GET /api/availability/shop/{shopId}/date/{date}
     * Access: PUBLIC (no authentication required)
     * 
     * WORKFLOW:
     * 1. Validate shop ID and date format
     * 2. Generate time slots based on shop hours
     * 3. Check existing appointments
     * 4. Return list of available/booked slots
     * 
     * WHY PUBLIC?
     * - Customers should see availability BEFORE creating account
     * - Encourages bookings (low friction)
     * 
     * EXAMPLE REQUEST:
     * GET /api/availability/shop/5/date/2025-11-15
     * (No Authorization header needed)
     * 
     * EXAMPLE RESPONSE (200 OK):
     * [
     *   {"time": "09:00", "available": true, "reason": "available"},
     *   {"time": "09:30", "available": false, "reason": "booked"},
     *   {"time": "10:00", "available": true, "reason": "available"},
     *   {"time": "10:30", "available": true, "reason": "available"}
     * ]
     * 
     * DATE FORMAT:
     * - Must be ISO date format: YYYY-MM-DD
     * - Example: 2025-11-15 (November 15, 2025)
     * - Spring automatically converts to LocalDate
     */
    @GetMapping("/availability/shop/{shopId}/date/{date}")
    public ResponseEntity<List<TimeSlotDto>> getAvailability(
        @PathVariable Long shopId,
        @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        @RequestParam(name = "employeeId", required = false) Long employeeId
    ) {
        log.info("Checking availability for shop {} on {} (employeeId={})", shopId, date, employeeId);

        // Call availability service to generate slots (optionally filtered by employee)
        List<TimeSlotDto> slots = availabilityService.getAvailableSlots(shopId, date, employeeId);

        log.info("Found {} time slots for shop {} on {} ({} available, employeeId={})", 
            slots.size(), 
            shopId, 
            date,
            slots.stream().filter(TimeSlotDto::available).count(),
            employeeId);

        return ResponseEntity.ok(slots);  // 200 OK
    }

    /**
     * CANCEL APPOINTMENT
     * Endpoint: PUT /api/appointments/{id}/cancel
     * Access: CUSTOMER role only (must own the appointment)
     */
    @PutMapping("/appointments/{id}/cancel")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<AppointmentResponseDto> cancelAppointment(
        @PathVariable("id") Long appointmentId,
        @AuthenticationPrincipal Jwt jwt
    ) {
        Long customerId = jwt.getClaim("userId");
        log.info("Customer {} cancelling appointment {}", customerId, appointmentId);
        AppointmentResponseDto result = appointmentService.cancelAppointment(appointmentId, customerId);
        return ResponseEntity.ok(result);
    }

    /**
     * RESCHEDULE APPOINTMENT
     * Endpoint: PUT /api/appointments/{id}/reschedule
     * Access: CUSTOMER role only (must own the appointment)
     */
    @PutMapping("/appointments/{id}/reschedule")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<AppointmentResponseDto> rescheduleAppointment(
        @PathVariable("id") Long appointmentId,
        @Valid @RequestBody AppointmentRescheduleRequestDto request,
        @AuthenticationPrincipal Jwt jwt
    ) {
        Long customerId = jwt.getClaim("userId");
        log.info("Customer {} rescheduling appointment {}", customerId, appointmentId);
        AppointmentResponseDto result = appointmentService.rescheduleAppointment(appointmentId, customerId, request);
        return ResponseEntity.ok(result);
    }

    /**
     * GET SINGLE APPOINTMENT (for edit mode prefill)
     * Endpoint: GET /api/appointments/{id}
     * Access: CUSTOMER role only (must own the appointment)
     */
    @GetMapping("/appointments/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<AppointmentResponseDto> getAppointmentById(
        @PathVariable("id") Long appointmentId,
        @AuthenticationPrincipal Jwt jwt
    ) {
        Long customerId = jwt.getClaim("userId");
        AppointmentResponseDto appt = appointmentService.getAppointmentForCustomer(appointmentId, customerId);
        return ResponseEntity.ok(appt);
    }

    /**
     * GLOBAL EXCEPTION HANDLER (Optional)
     * 
     * Handles common errors and returns user-friendly messages.
     * This is a simplified version - you can expand it later.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Error processing request: {}", e.getMessage(), e);
        
        // Return generic error message (don't expose internal details)
        ErrorResponse error = new ErrorResponse(
            "An error occurred processing your request",
            e.getMessage()
        );
        
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(error);
    }

    /**
     * Simple error response DTO for consistent error format.
     */
    public record ErrorResponse(
        String message,
        String details
    ) {}
}
