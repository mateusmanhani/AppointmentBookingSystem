-- User Service Seed Data
-- Password for all users: "Egmqr123k*" (BCrypt encoded)

-- IMPORTANT: To load this data, temporarily set spring.sql.init.mode=always in application-local.yml
-- After first successful load, set it back to 'never' to prevent re-execution on restart
-- NOTE: With mode=always, data will be reset on every restart (useful for testing with dynamic dates)

-- Clear existing data (uncomment if you need to reset)
DELETE FROM users WHERE id > 0;

-- Reset auto-increment
ALTER TABLE users AUTO_INCREMENT = 1;

-- Shop Owners (IDs 1-15)
INSERT INTO users (email, password, role, first_name, last_name, phone, is_active, created_at, updated_at) VALUES
('owner1@barbershop.ie', '$2a$12$aOvmhzdxSx1/9ahB9CndpuWS7PLOYYnf6ZPiPlc14GsutlgucUCWm', 'SHOP_OWNER', 'Liam', 'Murphy', '+353 1 234 5001', true, NOW(), NOW()),
('owner2@barbershop.ie', '$2a$12$aOvmhzdxSx1/9ahB9CndpuWS7PLOYYnf6ZPiPlc14GsutlgucUCWm', 'SHOP_OWNER', 'Sean', 'Kelly', '+353 1 234 5002', true, NOW(), NOW()),
('owner3@barbershop.ie', '$2a$12$aOvmhzdxSx1/9ahB9CndpuWS7PLOYYnf6ZPiPlc14GsutlgucUCWm', 'SHOP_OWNER', 'Connor', 'Walsh', '+353 1 234 5003', true, NOW(), NOW()),
('owner4@barbershop.ie', '$2a$12$aOvmhzdxSx1/9ahB9CndpuWS7PLOYYnf6ZPiPlc14GsutlgucUCWm', 'SHOP_OWNER', 'Oisin', 'Ryan', '+353 1 234 5004', true, NOW(), NOW()),
('owner5@barbershop.ie', '$2a$12$aOvmhzdxSx1/9ahB9CndpuWS7PLOYYnf6ZPiPlc14GsutlgucUCWm', 'SHOP_OWNER', 'Cian', "O'Sullivan", '+353 1 234 5005', true, NOW(), NOW()),
('owner6@barbershop.ie', '$2a$12$aOvmhzdxSx1/9ahB9CndpuWS7PLOYYnf6ZPiPlc14GsutlgucUCWm', 'SHOP_OWNER', 'Darragh', "O'Brien", '+353 1 234 5006', true, NOW(), NOW()),
('owner7@barbershop.ie', '$2a$12$aOvmhzdxSx1/9ahB9CndpuWS7PLOYYnf6ZPiPlc14GsutlgucUCWm', 'SHOP_OWNER', 'Paddy', 'McCarthy', '+353 1 234 5007', true, NOW(), NOW()),
('owner8@barbershop.ie', '$2a$12$aOvmhzdxSx1/9ahB9CndpuWS7PLOYYnf6ZPiPlc14GsutlgucUCWm', 'SHOP_OWNER', 'Eoin', 'Byrne', '+353 1 234 5008', true, NOW(), NOW()),
('owner9@barbershop.ie', '$2a$12$aOvmhzdxSx1/9ahB9CndpuWS7PLOYYnf6ZPiPlc14GsutlgucUCWm', 'SHOP_OWNER', 'Ronan', 'Doyle', '+353 1 234 5009', true, NOW(), NOW()),
('owner10@barbershop.ie', '$2a$12$aOvmhzdxSx1/9ahB9CndpuWS7PLOYYnf6ZPiPlc14GsutlgucUCWm', 'SHOP_OWNER', 'Ciaran', 'Kennedy', '+353 1 234 5010', true, NOW(), NOW()),
('owner11@barbershop.ie', '$2a$12$aOvmhzdxSx1/9ahB9CndpuWS7PLOYYnf6ZPiPlc14GsutlgucUCWm', 'SHOP_OWNER', 'Declan', 'Quinn', '+353 1 234 5011', true, NOW(), NOW()),
('owner12@barbershop.ie', '$2a$12$aOvmhzdxSx1/9ahB9CndpuWS7PLOYYnf6ZPiPlc14GsutlgucUCWm', 'SHOP_OWNER', 'Brendan', 'Lynch', '+353 1 234 5012', true, NOW(), NOW()),
('owner13@barbershop.ie', '$2a$12$aOvmhzdxSx1/9ahB9CndpuWS7PLOYYnf6ZPiPlc14GsutlgucUCWm', 'SHOP_OWNER', 'Aidan', 'Murray', '+353 1 234 5013', true, NOW(), NOW()),
('owner14@barbershop.ie', '$2a$12$aOvmhzdxSx1/9ahB9CndpuWS7PLOYYnf6ZPiPlc14GsutlgucUCWm', 'SHOP_OWNER', 'Niall', 'Brennan', '+353 1 234 5014', true, NOW(), NOW()),
('owner15@barbershop.ie', '$2a$12$aOvmhzdxSx1/9ahB9CndpuWS7PLOYYnf6ZPiPlc14GsutlgucUCWm', 'SHOP_OWNER', 'Tadhg', 'Dunne', '+353 1 234 5015', true, NOW(), NOW());

-- Customers (IDs 16-35)
INSERT INTO users (email, password, role, first_name, last_name, phone, is_active, created_at, updated_at) VALUES
('james.wilson@email.ie', '$2a$12$aOvmhzdxSx1/9ahB9CndpuWS7PLOYYnf6ZPiPlc14GsutlgucUCWm', 'CUSTOMER', 'James', 'Wilson', '+353 87 123 4501', true, NOW(), NOW()),
('emma.brown@email.ie', '$2a$12$aOvmhzdxSx1/9ahB9CndpuWS7PLOYYnf6ZPiPlc14GsutlgucUCWm', 'CUSTOMER', 'Emma', 'Brown', '+353 87 123 4502', true, NOW(), NOW()),
('oliver.jones@email.ie', '$2a$12$aOvmhzdxSx1/9ahB9CndpuWS7PLOYYnf6ZPiPlc14GsutlgucUCWm', 'CUSTOMER', 'Oliver', 'Jones', '+353 87 123 4503', true, NOW(), NOW()),
('sophia.davis@email.ie', '$2a$12$aOvmhzdxSx1/9ahB9CndpuWS7PLOYYnf6ZPiPlc14GsutlgucUCWm', 'CUSTOMER', 'Sophia', 'Davis', '+353 87 123 4504', true, NOW(), NOW()),
('harry.taylor@email.ie', '$2a$12$aOvmhzdxSx1/9ahB9CndpuWS7PLOYYnf6ZPiPlc14GsutlgucUCWm', 'CUSTOMER', 'Harry', 'Taylor', '+353 87 123 4505', true, NOW(), NOW()),
('amelia.smith@email.ie', '$2a$12$aOvmhzdxSx1/9ahB9CndpuWS7PLOYYnf6ZPiPlc14GsutlgucUCWm', 'CUSTOMER', 'Amelia', 'Smith', '+353 87 123 4506', true, NOW(), NOW()),
('jack.martin@email.ie', '$2a$12$aOvmhzdxSx1/9ahB9CndpuWS7PLOYYnf6ZPiPlc14GsutlgucUCWm', 'CUSTOMER', 'Jack', 'Martin', '+353 87 123 4507', true, NOW(), NOW()),
('isla.harris@email.ie', '$2a$12$aOvmhzdxSx1/9ahB9CndpuWS7PLOYYnf6ZPiPlc14GsutlgucUCWm', 'CUSTOMER', 'Isla', 'Harris', '+353 87 123 4508', true, NOW(), NOW()),
('charlie.white@email.ie', '$2a$12$aOvmhzdxSx1/9ahB9CndpuWS7PLOYYnf6ZPiPlc14GsutlgucUCWm', 'CUSTOMER', 'Charlie', 'White', '+353 87 123 4509', true, NOW(), NOW()),
('mia.thompson@email.ie', '$2a$12$aOvmhzdxSx1/9ahB9CndpuWS7PLOYYnf6ZPiPlc14GsutlgucUCWm', 'CUSTOMER', 'Mia', 'Thompson', '+353 87 123 4510', true, NOW(), NOW()),
('thomas.clarke@email.ie', '$2a$12$aOvmhzdxSx1/9ahB9CndpuWS7PLOYYnf6ZPiPlc14GsutlgucUCWm', 'CUSTOMER', 'Thomas', 'Clarke', '+353 87 123 4511', true, NOW(), NOW()),
('grace.evans@email.ie', '$2a$12$aOvmhzdxSx1/9ahB9CndpuWS7PLOYYnf6ZPiPlc14GsutlgucUCWm', 'CUSTOMER', 'Grace', 'Evans', '+353 87 123 4512', true, NOW(), NOW()),
('george.moore@email.ie', '$2a$12$aOvmhzdxSx1/9ahB9CndpuWS7PLOYYnf6ZPiPlc14GsutlgucUCWm', 'CUSTOMER', 'George', 'Moore', '+353 87 123 4513', true, NOW(), NOW()),
('lily.walker@email.ie', '$2a$12$aOvmhzdxSx1/9ahB9CndpuWS7PLOYYnf6ZPiPlc14GsutlgucUCWm', 'CUSTOMER', 'Lily', 'Walker', '+353 87 123 4514', true, NOW(), NOW()),
('noah.robinson@email.ie', '$2a$12$aOvmhzdxSx1/9ahB9CndpuWS7PLOYYnf6ZPiPlc14GsutlgucUCWm', 'CUSTOMER', 'Noah', 'Robinson', '+353 87 123 4515', true, NOW(), NOW()),
('emily.king@email.ie', '$2a$12$aOvmhzdxSx1/9ahB9CndpuWS7PLOYYnf6ZPiPlc14GsutlgucUCWm', 'CUSTOMER', 'Emily', 'King', '+353 87 123 4516', true, NOW(), NOW()),
('oscar.baker@email.ie', '$2a$12$aOvmhzdxSx1/9ahB9CndpuWS7PLOYYnf6ZPiPlc14GsutlgucUCWm', 'CUSTOMER', 'Oscar', 'Baker', '+353 87 123 4517', true, NOW(), NOW()),
('ava.green@email.ie', '$2a$12$aOvmhzdxSx1/9ahB9CndpuWS7PLOYYnf6ZPiPlc14GsutlgucUCWm', 'CUSTOMER', 'Ava', 'Green', '+353 87 123 4518', true, NOW(), NOW()),
('leo.wright@email.ie', '$2a$12$aOvmhzdxSx1/9ahB9CndpuWS7PLOYYnf6ZPiPlc14GsutlgucUCWm', 'CUSTOMER', 'Leo', 'Wright', '+353 87 123 4519', true, NOW(), NOW()),
('freya.hill@email.ie', '$2a$12$aOvmhzdxSx1/9ahB9CndpuWS7PLOYYnf6ZPiPlc14GsutlgucUCWm', 'CUSTOMER', 'Freya', 'Hill', '+353 87 123 4520', true, NOW(), NOW());
