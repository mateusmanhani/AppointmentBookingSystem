-- =============================================
-- Appointment Service Database Migration
-- =============================================
-- Database: appointment_db
-- Description: Stores customer appointments with barbershops
-- =============================================

-- Create database (if not exists)
CREATE DATABASE IF NOT EXISTS appointment_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE appointment_db;

-- =============================================
-- Table: appointments
-- =============================================
-- Stores customer appointments at barbershops.
-- Uses foreign keys (IDs only) to reference:
--   - User (customer_id) from user-service
--   - Shop (shop_id) from shop-service  
--   - Service (service_id) from shop-service
--   - Employee (employee_id) from shop-service
-- 
-- Full details are fetched at runtime to avoid duplication.
-- =============================================

CREATE TABLE IF NOT EXISTS appointments (
    -- Primary Key
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    
    -- Foreign Keys (References to other services)
    customer_id BIGINT NOT NULL COMMENT 'References user in user-service',
    shop_id BIGINT NOT NULL COMMENT 'References shop in shop-service',
    service_id BIGINT NOT NULL COMMENT 'References service in shop-service',
    employee_id BIGINT NULL COMMENT 'References employee in shop-service (NULL = any available)',
    
    -- Appointment Details
    appointment_date DATE NOT NULL COMMENT 'Date of the appointment',
    appointment_time TIME NOT NULL COMMENT 'Time of the appointment (24-hour format)',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING, CONFIRMED, CANCELLED, COMPLETED, NO_SHOW',
    notes TEXT NULL COMMENT 'Customer notes or special requests',
    
    -- Audit Fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'When appointment was created',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update time',
    
    -- Constraints
    CONSTRAINT chk_status CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED', 'NO_SHOW')),
    CONSTRAINT chk_appointment_date CHECK (appointment_date >= CURRENT_DATE)
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Customer appointments at barbershops';

-- =============================================
-- Indexes for Performance
-- =============================================

-- Customer lookup: Find all appointments for a customer, ordered by date
CREATE INDEX idx_customer_date ON appointments (customer_id, appointment_date);

-- Shop lookup: Find all appointments for a shop on a specific date/time
CREATE INDEX idx_shop_date ON appointments (shop_id, appointment_date, appointment_time);

-- Employee schedule: Find all appointments for an employee on a date
CREATE INDEX idx_employee_date ON appointments (employee_id, appointment_date);

-- Status filtering: Filter appointments by status (e.g., pending, confirmed)
CREATE INDEX idx_status ON appointments (status);

-- Shop owner dashboard: Find appointments by shop, status, and date
CREATE INDEX idx_shop_status_date ON appointments (shop_id, status, appointment_date);

-- =============================================
-- Sample Data (Optional - for testing)
-- =============================================

-- Note: Ensure the referenced IDs exist in other services
-- Uncomment below to insert sample data:

/*
INSERT INTO appointments (customer_id, shop_id, service_id, employee_id, appointment_date, appointment_time, status, notes)
VALUES 
    (1, 1, 1, 1, '2025-11-15', '09:00:00', 'PENDING', 'First time customer'),
    (1, 1, 2, 2, '2025-11-16', '14:30:00', 'CONFIRMED', 'Regular customer'),
    (2, 1, 1, 1, '2025-11-15', '10:00:00', 'CONFIRMED', NULL),
    (2, 2, 3, 3, '2025-11-17', '11:00:00', 'PENDING', 'Request specific barber'),
    (3, 1, 1, NULL, '2025-11-18', '15:00:00', 'PENDING', 'Any available employee');
*/

-- =============================================
-- Database User Setup (Optional)
-- =============================================

-- Create dedicated user for appointment-service
-- Adjust username and password as needed

/*
CREATE USER IF NOT EXISTS 'appointment_user'@'localhost' IDENTIFIED BY 'appointment_password';
GRANT SELECT, INSERT, UPDATE, DELETE ON appointment_db.* TO 'appointment_user'@'localhost';
FLUSH PRIVILEGES;
*/

-- =============================================
-- Verification Queries
-- =============================================

-- Check table structure
-- DESCRIBE appointments;

-- Count total appointments
-- SELECT COUNT(*) as total_appointments FROM appointments;

-- Show status distribution
-- SELECT status, COUNT(*) as count FROM appointments GROUP BY status;

-- Find today's appointments
-- SELECT * FROM appointments WHERE appointment_date = CURRENT_DATE ORDER BY appointment_time;

-- =============================================
-- Maintenance Queries
-- =============================================

-- Archive old appointments (older than 1 year)
-- This is a manual operation - consider running periodically
/*
-- Create archive table first
CREATE TABLE IF NOT EXISTS appointments_archive LIKE appointments;

-- Copy old records
INSERT INTO appointments_archive
SELECT * FROM appointments
WHERE appointment_date < DATE_SUB(CURRENT_DATE, INTERVAL 1 YEAR);

-- Delete archived records from main table
DELETE FROM appointments
WHERE appointment_date < DATE_SUB(CURRENT_DATE, INTERVAL 1 YEAR);
*/

-- =============================================
-- Performance Notes
-- =============================================
-- 1. The indexes are optimized for common query patterns:
--    - Customer viewing their appointments
--    - Shop owner viewing daily schedule
--    - Availability checking (shop + date)
--    - Employee schedule viewing
--
-- 2. Consider partitioning by appointment_date if table grows large (millions of rows)
--
-- 3. Regularly analyze and optimize:
--    ANALYZE TABLE appointments;
--    OPTIMIZE TABLE appointments;
--
-- 4. Monitor query performance:
--    EXPLAIN SELECT * FROM appointments WHERE shop_id = 1 AND appointment_date = '2025-11-15';
-- =============================================
