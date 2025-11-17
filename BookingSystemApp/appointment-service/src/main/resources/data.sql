-- Appointment Service Seed Data
-- Sample appointments across all Dublin shops with varied statuses and dates
-- NOW WITH service_duration_minutes: Stores service duration at booking time (snapshot pattern)

-- IMPORTANT: To load this data, temporarily set spring.sql.init.mode=always in application.yaml
-- After first successful load, set it back to 'never' to prevent re-execution on restart

-- Clear existing data (ensures clean reload if needed)
DELETE FROM appointments WHERE id > 0;

-- Reset auto-increment
ALTER TABLE appointments AUTO_INCREMENT = 1;

-- ========================================
-- APPOINTMENTS
-- Mix of past (completed/cancelled), current (confirmed), and future (pending) appointments
-- Customer IDs: 16-35, Shop IDs: 1-15, Service IDs vary by shop, Employee IDs vary by shop
-- Each appointment now includes service_duration_minutes for proper time slot blocking
-- ========================================

-- Past Appointments (Completed) - 2 weeks ago
INSERT INTO appointments (customer_id, shop_id, service_id, employee_id, appointment_date, appointment_time, status, notes, service_duration_minutes, created_at, updated_at) VALUES
-- Shop 1: The Grafton Barber
(16, 1, 1, 1, DATE_SUB(CURDATE(), INTERVAL 14 DAY), '10:00:00', 'COMPLETED', 'Great service, very professional', 30, DATE_SUB(NOW(), INTERVAL 14 DAY), DATE_SUB(NOW(), INTERVAL 14 DAY)),
(17, 1, 4, 2, DATE_SUB(CURDATE(), INTERVAL 13 DAY), '14:30:00', 'COMPLETED', 'Amazing hot towel shave', 45, DATE_SUB(NOW(), INTERVAL 13 DAY), DATE_SUB(NOW(), INTERVAL 13 DAY)),
(18, 1, 2, 3, DATE_SUB(CURDATE(), INTERVAL 12 DAY), '11:00:00', 'COMPLETED', NULL, 40, DATE_SUB(NOW(), INTERVAL 12 DAY), DATE_SUB(NOW(), INTERVAL 12 DAY)),

-- Shop 2: Northside Cuts
(19, 2, 6, 4, DATE_SUB(CURDATE(), INTERVAL 14 DAY), '09:00:00', 'COMPLETED', 'My son loved the cut!', 25, DATE_SUB(NOW(), INTERVAL 14 DAY), DATE_SUB(NOW(), INTERVAL 14 DAY)),
(20, 2, 7, 5, DATE_SUB(CURDATE(), INTERVAL 11 DAY), '15:00:00', 'COMPLETED', 'Perfect fade', 20, DATE_SUB(NOW(), INTERVAL 11 DAY), DATE_SUB(NOW(), INTERVAL 11 DAY)),
(21, 2, 10, 6, DATE_SUB(CURDATE(), INTERVAL 10 DAY), '10:30:00', 'COMPLETED', 'Excellent father-son package', 50, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 10 DAY)),

-- Shop 3: Rathmines Barber Co.
(22, 3, 11, 7, DATE_SUB(CURDATE(), INTERVAL 13 DAY), '11:30:00', 'COMPLETED', 'Love the modern vibe', 35, DATE_SUB(NOW(), INTERVAL 13 DAY), DATE_SUB(NOW(), INTERVAL 13 DAY)),
(23, 3, 13, 8, DATE_SUB(CURDATE(), INTERVAL 9 DAY), '16:00:00', 'COMPLETED', NULL, 45, DATE_SUB(NOW(), INTERVAL 9 DAY), DATE_SUB(NOW(), INTERVAL 9 DAY)),

-- Shop 4: Ballsbridge Barbers
(24, 4, 16, 10, DATE_SUB(CURDATE(), INTERVAL 10 DAY), '10:00:00', 'COMPLETED', 'Luxury experience worth every euro', 30, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 10 DAY)),
(25, 4, 20, 11, DATE_SUB(CURDATE(), INTERVAL 8 DAY), '14:00:00', 'COMPLETED', 'Best shave in Dublin', 75, DATE_SUB(NOW(), INTERVAL 8 DAY), DATE_SUB(NOW(), INTERVAL 8 DAY));

-- Past Appointments (Cancelled) - 1 week ago
INSERT INTO appointments (customer_id, shop_id, service_id, employee_id, appointment_date, appointment_time, status, notes, service_duration_minutes, created_at, updated_at) VALUES
(26, 5, 21, 13, DATE_SUB(CURDATE(), INTERVAL 7 DAY), '09:30:00', 'CANCELLED', 'Had to cancel, family emergency', 30, DATE_SUB(NOW(), INTERVAL 8 DAY), DATE_SUB(NOW(), INTERVAL 7 DAY)),
(27, 6, 26, 16, DATE_SUB(CURDATE(), INTERVAL 6 DAY), '17:00:00', 'CANCELLED', 'Couldn''t make it', 25, DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 6 DAY)),
(28, 7, 31, 19, DATE_SUB(CURDATE(), INTERVAL 5 DAY), '12:00:00', 'CANCELLED', 'Rescheduled for next week', 35, DATE_SUB(NOW(), INTERVAL 6 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY));

-- Recent Past Appointments (Completed) - Last few days
INSERT INTO appointments (customer_id, shop_id, service_id, employee_id, appointment_date, appointment_time, status, notes, service_duration_minutes, created_at, updated_at) VALUES
(29, 8, 36, 24, DATE_SUB(CURDATE(), INTERVAL 4 DAY), '11:00:00', 'COMPLETED', 'Sea view was amazing', 30, DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY)),
(30, 9, 41, 27, DATE_SUB(CURDATE(), INTERVAL 3 DAY), '15:30:00', 'COMPLETED', 'Cool vintage style', 30, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)),
(31, 10, 46, 30, DATE_SUB(CURDATE(), INTERVAL 2 DAY), '10:30:00', 'COMPLETED', 'Espresso was perfect', 25, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
(32, 11, 51, 33, DATE_SUB(CURDATE(), INTERVAL 1 DAY), '14:00:00', 'COMPLETED', 'Very distinguished service', 35, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY));

-- Today's Appointments (Confirmed)
INSERT INTO appointments (customer_id, shop_id, service_id, employee_id, appointment_date, appointment_time, status, notes, service_duration_minutes, created_at, updated_at) VALUES
(33, 12, 56, 37, CURDATE(), '09:00:00', 'CONFIRMED', 'First time at this shop', 25, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(34, 13, 61, 41, CURDATE(), '11:30:00', 'CONFIRMED', NULL, 30, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(35, 14, 66, 44, CURDATE(), '14:00:00', 'CONFIRMED', 'Regular customer', 25, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
(16, 15, 71, 48, CURDATE(), '16:30:00', 'CONFIRMED', NULL, 35, NOW(), NOW());

-- Tomorrow's Appointments (Confirmed & Pending)
INSERT INTO appointments (customer_id, shop_id, service_id, employee_id, appointment_date, appointment_time, status, notes, service_duration_minutes, created_at, updated_at) VALUES
(17, 1, 5, 1, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '10:00:00', 'CONFIRMED', 'Looking forward to full service package', 75, NOW(), NOW()),
(18, 2, 8, 5, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '11:00:00', 'CONFIRMED', NULL, 35, NOW(), NOW()),
(19, 3, 12, 7, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '13:30:00', 'PENDING', 'Need confirmation call', 30, NOW(), NOW()),
(20, 4, 19, 11, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '15:00:00', 'CONFIRMED', 'Executive meeting after', 45, NOW(), NOW()),
(21, 5, 23, 14, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '09:30:00', 'PENDING', NULL, 45, NOW(), NOW());

-- Future Appointments - 2-7 days ahead (Confirmed & Pending)
INSERT INTO appointments (customer_id, shop_id, service_id, employee_id, appointment_date, appointment_time, status, notes, service_duration_minutes, created_at, updated_at) VALUES
-- 2 days ahead
(22, 6, 30, 18, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '10:30:00', 'CONFIRMED', 'Will have craft beer', 75, NOW(), NOW()),
(23, 7, 32, 20, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '14:00:00', 'PENDING', 'Student discount please', 30, NOW(), NOW()),
(24, 8, 40, 25, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '16:00:00', 'CONFIRMED', NULL, 75, NOW(), NOW()),

-- 3 days ahead
(25, 9, 45, 29, DATE_ADD(CURDATE(), INTERVAL 3 DAY), '11:00:00', 'CONFIRMED', 'Want the vinyl experience', 60, NOW(), NOW()),
(26, 10, 50, 32, DATE_ADD(CURDATE(), INTERVAL 3 DAY), '13:30:00', 'PENDING', NULL, 50, NOW(), NOW()),
(27, 11, 55, 35, DATE_ADD(CURDATE(), INTERVAL 3 DAY), '10:00:00', 'CONFIRMED', 'Business trip to Dublin', 60, NOW(), NOW()),

-- 4 days ahead
(28, 12, 59, 39, DATE_ADD(CURDATE(), INTERVAL 4 DAY), '15:00:00', 'CONFIRMED', NULL, 45, NOW(), NOW()),
(29, 13, 65, 43, DATE_ADD(CURDATE(), INTERVAL 4 DAY), '12:00:00', 'PENDING', 'Beach walk after', 60, NOW(), NOW()),
(30, 14, 70, 47, DATE_ADD(CURDATE(), INTERVAL 4 DAY), '09:00:00', 'CONFIRMED', 'Traditional cut please', 50, NOW(), NOW()),

-- 5 days ahead
(31, 15, 75, 50, DATE_ADD(CURDATE(), INTERVAL 5 DAY), '11:30:00', 'CONFIRMED', 'Weekend appointment', 75, NOW(), NOW()),
(32, 1, 2, 2, DATE_ADD(CURDATE(), INTERVAL 5 DAY), '14:30:00', 'PENDING', 'Special fade request', 40, NOW(), NOW()),
(33, 2, 9, 7, DATE_ADD(CURDATE(), INTERVAL 5 DAY), '16:00:00', 'CONFIRMED', NULL, 15, NOW(), NOW()),

-- 6 days ahead
(34, 3, 15, 9, DATE_ADD(CURDATE(), INTERVAL 6 DAY), '10:00:00', 'CONFIRMED', 'Combination service', 60, NOW(), NOW()),
(35, 4, 17, 12, DATE_ADD(CURDATE(), INTERVAL 6 DAY), '13:00:00', 'PENDING', 'First visit to D4 shop', 40, NOW(), NOW()),
(16, 5, 25, 15, DATE_ADD(CURDATE(), INTERVAL 6 DAY), '15:30:00', 'CONFIRMED', 'Senior special', 75, NOW(), NOW()),

-- 7 days ahead (1 week from now)
(17, 6, 27, 17, DATE_ADD(CURDATE(), INTERVAL 7 DAY), '11:00:00', 'CONFIRMED', 'Saturday appointment', 35, NOW(), NOW()),
(18, 7, 35, 22, DATE_ADD(CURDATE(), INTERVAL 7 DAY), '14:00:00', 'PENDING', NULL, 75, NOW(), NOW()),
(19, 8, 39, 26, DATE_ADD(CURDATE(), INTERVAL 7 DAY), '10:30:00', 'CONFIRMED', 'Traditional shave', 45, NOW(), NOW()),
(20, 9, 44, 28, DATE_ADD(CURDATE(), INTERVAL 7 DAY), '16:00:00', 'CONFIRMED', 'Buzz and fade combo', 20, NOW(), NOW());

-- Future Appointments - 2-4 weeks ahead (Planning ahead)
INSERT INTO appointments (customer_id, shop_id, service_id, employee_id, appointment_date, appointment_time, status, notes, service_duration_minutes, created_at, updated_at) VALUES
-- 2 weeks ahead
(21, 10, 48, 31, DATE_ADD(CURDATE(), INTERVAL 14 DAY), '13:00:00', 'PENDING', 'Monthly appointment', 40, NOW(), NOW()),
(22, 11, 52, 34, DATE_ADD(CURDATE(), INTERVAL 14 DAY), '10:00:00', 'PENDING', 'Executive service booking', 30, NOW(), NOW()),
(23, 12, 58, 40, DATE_ADD(CURDATE(), INTERVAL 15 DAY), '15:00:00', 'PENDING', NULL, 40, NOW(), NOW()),

-- 3 weeks ahead
(24, 13, 63, 42, DATE_ADD(CURDATE(), INTERVAL 21 DAY), '11:30:00', 'PENDING', 'Family appointment', 45, NOW(), NOW()),
(25, 14, 68, 46, DATE_ADD(CURDATE(), INTERVAL 21 DAY), '14:00:00', 'PENDING', 'Heritage package interest', 40, NOW(), NOW()),
(26, 15, 73, 49, DATE_ADD(CURDATE(), INTERVAL 22 DAY), '10:00:00', 'PENDING', 'Coastal fade', 45, NOW(), NOW()),

-- 4 weeks ahead (1 month)
(27, 1, 3, 3, DATE_ADD(CURDATE(), INTERVAL 28 DAY), '12:00:00', 'PENDING', 'Advance booking', 20, NOW(), NOW()),
(28, 2, 7, 6, DATE_ADD(CURDATE(), INTERVAL 28 DAY), '15:30:00', 'PENDING', NULL, 20, NOW(), NOW()),
(29, 3, 14, 8, DATE_ADD(CURDATE(), INTERVAL 29 DAY), '11:00:00', 'PENDING', 'Buzz cut monthly', 20, NOW(), NOW()),
(30, 4, 18, 10, DATE_ADD(CURDATE(), INTERVAL 30 DAY), '13:30:00', 'PENDING', 'Texture styling session', 40, NOW(), NOW());

-- Additional varied appointments for demo richness
INSERT INTO appointments (customer_id, shop_id, service_id, employee_id, appointment_date, appointment_time, status, notes, service_duration_minutes, created_at, updated_at) VALUES
-- More today/tomorrow for availability testing
(31, 6, 29, 16, CURDATE(), '12:00:00', 'CONFIRMED', NULL, 45, NOW(), NOW()),
(32, 7, 34, 21, CURDATE(), '17:00:00', 'CONFIRMED', 'After work', 45, NOW(), NOW()),
(33, 8, 37, 24, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '09:00:00', 'CONFIRMED', 'Early morning', 40, NOW(), NOW()),
(34, 9, 42, 27, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '17:30:00', 'PENDING', 'Evening slot', 40, NOW(), NOW()),

-- Various shops next week for map testing
(35, 10, 47, 30, DATE_ADD(CURDATE(), INTERVAL 8 DAY), '10:00:00', 'PENDING', NULL, 35, NOW(), NOW()),
(16, 11, 54, 33, DATE_ADD(CURDATE(), INTERVAL 9 DAY), '14:00:00', 'PENDING', 'Second visit', 45, NOW(), NOW()),
(17, 12, 60, 38, DATE_ADD(CURDATE(), INTERVAL 10 DAY), '11:30:00', 'PENDING', NULL, 75, NOW(), NOW()),
(18, 13, 64, 41, DATE_ADD(CURDATE(), INTERVAL 11 DAY), '15:00:00', 'PENDING', 'Beach appointment', 20, NOW(), NOW()),
(19, 14, 69, 45, DATE_ADD(CURDATE(), INTERVAL 12 DAY), '13:00:00', 'PENDING', 'Traditional service', 45, NOW(), NOW()),
(20, 15, 74, 48, DATE_ADD(CURDATE(), INTERVAL 13 DAY), '16:00:00', 'PENDING', 'Clippers special', 20, NOW(), NOW());
