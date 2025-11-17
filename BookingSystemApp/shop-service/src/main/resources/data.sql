-- Shop Service Seed Data
-- Comprehensive Dublin Barbershops with real locations and GPS coordinates

-- IMPORTANT: To load this data, temporarily set spring.sql.init.mode=always in application.yml
-- After first successful load, set it back to 'never' to prevent re-execution on restart

-- Clear existing data (these DELETEs ensure clean reload if needed)
DELETE FROM services WHERE id > 0;
DELETE FROM employees WHERE id > 0;
DELETE FROM shops WHERE id > 0;

-- Reset auto-increment
ALTER TABLE shops AUTO_INCREMENT = 1;
ALTER TABLE employees AUTO_INCREMENT = 1;
ALTER TABLE services AUTO_INCREMENT = 1;

-- ========================================
-- SHOPS (15 Dublin locations with real coordinates)
-- ========================================

INSERT INTO shops (name, address, city, state, zip_code, phone, description, opening_time, closing_time, owner_id, latitude, longitude, created_at, updated_at) VALUES
-- 1. Temple Bar area (city center)
('The Grafton Barber', '45 South William Street', 'Dublin', 'Leinster', 'D02 R292', '+353 1 679 5001', 'Premium barbering in the heart of Dublin''s creative quarter. Traditional wet shaves and modern styles.', '09:00', '19:00', 1, 53.342778, -6.264167, NOW(), NOW()),

-- 2. O''Connell Street area
('Northside Cuts', '28 O''Connell Street Upper', 'Dublin', 'Leinster', 'D01 A3X2', '+353 1 874 5002', 'Family-friendly barbershop serving Dublin 1 for over 20 years. Walk-ins welcome.', '08:30', '18:30', 2, 53.351389, -6.260556, NOW(), NOW()),

-- 3. Rathmines area (south)
('Rathmines Barber Co.', '156 Rathmines Road Lower', 'Dublin', 'Leinster', 'D06 E0C7', '+353 1 496 5003', 'Modern barbershop with vintage charm. Specializing in fades, beard sculpting, and traditional cuts.', '09:00', '20:00', 3, 53.325000, -6.265833, NOW(), NOW()),

-- 4. Ballsbridge area (affluent south)
('Ballsbridge Barbers', '42 Pembroke Road', 'Dublin', 'Leinster', 'D04 FP27', '+353 1 668 5004', 'Luxury grooming experience in Dublin 4. Hot towel shaves and premium hair products.', '09:30', '18:00', 4, 53.330556, -6.237778, NOW(), NOW()),

-- 5. Drumcondra area (north)
('Drumcondra Grooming', '67 Drumcondra Road Lower', 'Dublin', 'Leinster', 'D09 KW82', '+353 1 837 5005', 'Local neighborhood barber with skilled staff. Great for kids'' cuts and gentleman''s grooming.', '08:00', '18:00', 5, 53.368333, -6.256667, NOW(), NOW()),

-- 6. Ranelagh area (trendy south)
('Ranelagh Razor', '12 Ranelagh Village', 'Dublin', 'Leinster', 'D06 C9V6', '+353 1 497 5006', 'Trendy barbershop in vibrant Ranelagh. Craft beer while you wait, music, and expert cuts.', '10:00', '19:00', 6, 53.322222, -6.253889, NOW(), NOW()),

-- 7. Phibsborough area (north)
('Phizzers Barbershop', '34 Phibsborough Road', 'Dublin', 'Leinster', 'D07 E1W0', '+353 1 830 5007', 'Community barbershop with affordable prices and friendly atmosphere. No appointment needed.', '09:00', '18:30', 7, 53.360833, -6.273333, NOW(), NOW()),

-- 8. Blackrock area (coastal south)
('Blackrock Barber House', '78 Main Street Blackrock', 'Dublin', 'Leinster', 'A94 R7W9', '+353 1 283 5008', 'Coastal Dublin''s premier barbershop. Sea-view grooming with top-tier service.', '09:00', '18:00', 8, 53.301111, -6.177778, NOW(), NOW()),

-- 9. Stoneybatter area (trendy north)
('Stoneybatter Style House', '23 Manor Street', 'Dublin', 'Leinster', 'D07 X9N3', '+353 1 670 5009', 'Hip neighborhood spot with vinyl records and vintage decor. Expert fades and beard trims.', '10:00', '19:00', 9, 53.354167, -6.284444, NOW(), NOW()),

-- 10. Portobello area (canal)
('Portobello Parlour', '67 South Circular Road', 'Dublin', 'Leinster', 'D08 Y443', '+353 1 475 5010', 'Canal-side barbering with a focus on precision and detail. Complimentary espresso.', '09:30', '19:30', 10, 53.334722, -6.269167, NOW(), NOW()),

-- 11. Donnybrook area (south)
('Donnybrook Distinguished Barbers', '12 Donnybrook Road', 'Dublin', 'Leinster', 'D04 Y8X3', '+353 1 269 5011', 'Sophisticated grooming for the discerning gentleman. Members club atmosphere.', '09:00', '18:30', 11, 53.317222, -6.233611, NOW(), NOW()),

-- 12. Smithfield area (revitalized north)
('Smithfield Square Barbers', '5 Smithfield Square', 'Dublin', 'Leinster', 'D07 X2P7', '+353 1 872 5012', 'Modern barbershop in historic Smithfield. Great for contemporary styles and beard work.', '08:30', '19:00', 12, 53.347500, -6.278611, NOW(), NOW()),

-- 13. Sandymount area (coastal)
('Sandymount Shore Barbers', '45 Sandymount Road', 'Dublin', 'Leinster', 'D04 YD56', '+353 1 269 5013', 'Relaxed coastal vibes with professional service. Family-run since 2010.', '09:00', '18:00', 13, 53.330000, -6.218056, NOW(), NOW()),

-- 14. Harold''s Cross area
('Harold''s Cross Hair Co.', '89 Harold''s Cross Road', 'Dublin', 'Leinster', 'D6W P220', '+353 1 492 5014', 'Traditional Irish barbershop with modern techniques. Three generations of barbers.', '08:00', '18:30', 14, 53.323333, -6.281111, NOW(), NOW()),

-- 15. Clontarf area (north coastal)
('Clontarf Clippers', '156 Clontarf Road', 'Dublin', 'Leinster', 'D03 A0P2', '+353 1 833 5015', 'Seaside barbering at its finest. Panoramic views and expert grooming services.', '09:00', '19:00', 15, 53.363889, -6.216667, NOW(), NOW());


-- ========================================
-- SERVICES (5 services per shop = 75 total)
-- ========================================

-- Shop 1: The Grafton Barber
INSERT INTO services (shop_id, name, description, price, duration_minutes, created_at, updated_at) VALUES
(1, 'Classic Cut', 'Traditional scissor cut with consultation and styling', 30.00, 30, NOW(), NOW()),
(1, 'Fade & Taper', 'Modern fade with clipper work and texture styling', 35.00, 40, NOW(), NOW()),
(1, 'Beard Trim & Shape', 'Professional beard sculpting with hot towel treatment', 20.00, 20, NOW(), NOW()),
(1, 'Deluxe Hot Towel Shave', 'Traditional wet shave with pre-shave oil and aftercare', 40.00, 45, NOW(), NOW()),
(1, 'Full Service Package', 'Cut, beard trim, and hot towel shave combo', 75.00, 75, NOW(), NOW());

-- Shop 2: Northside Cuts
INSERT INTO services (shop_id, name, description, price, duration_minutes, created_at, updated_at) VALUES
(2, 'Standard Haircut', 'Quick, professional cut for busy schedules', 25.00, 25, NOW(), NOW()),
(2, 'Kids Cut (Under 12)', 'Friendly service for children with patience and care', 18.00, 20, NOW(), NOW()),
(2, 'Skin Fade', 'Precision clipper fade to skin level', 32.00, 35, NOW(), NOW()),
(2, 'Beard Grooming', 'Trim, shape, and oil treatment', 18.00, 15, NOW(), NOW()),
(2, 'Father & Son Combo', 'Two haircuts, special family price', 40.00, 50, NOW(), NOW());

-- Shop 3: Rathmines Barber Co.
INSERT INTO services (shop_id, name, description, price, duration_minutes, created_at, updated_at) VALUES
(3, 'Signature Cut', 'Personalized cut with expert consultation', 32.00, 35, NOW(), NOW()),
(3, 'High Fade', 'Clean high fade with texture on top', 38.00, 40, NOW(), NOW()),
(3, 'Beard Sculpting', 'Detailed beard work with straight razor lines', 25.00, 25, NOW(), NOW()),
(3, 'Buzz Cut', 'Simple all-over clipper cut', 20.00, 15, NOW(), NOW()),
(3, 'Cut & Beard Combo', 'Full grooming service', 50.00, 60, NOW(), NOW());

-- Shop 4: Ballsbridge Barbers
INSERT INTO services (shop_id, name, description, price, duration_minutes, created_at, updated_at) VALUES
(4, 'Executive Cut', 'Premium haircut with luxury products', 45.00, 45, NOW(), NOW()),
(4, 'Gentleman''s Shave', 'Hot towel straight razor shave experience', 50.00, 50, NOW(), NOW()),
(4, 'Beard Maintenance', 'Precision trimming and conditioning', 30.00, 25, NOW(), NOW()),
(4, 'Texture Styling', 'Modern textured cut with styling session', 42.00, 40, NOW(), NOW()),
(4, 'Platinum Package', 'Cut, shave, and facial treatment', 95.00, 90, NOW(), NOW());

-- Shop 5: Drumcondra Grooming
INSERT INTO services (shop_id, name, description, price, duration_minutes, created_at, updated_at) VALUES
(5, 'Basic Haircut', 'Clean, simple cut', 22.00, 25, NOW(), NOW()),
(5, 'School Boy Cut', 'Smart cut for students', 16.00, 20, NOW(), NOW()),
(5, 'Fade Cut', 'Low, mid, or high fade options', 30.00, 35, NOW(), NOW()),
(5, 'Beard Trim', 'Quick beard tidy-up', 15.00, 15, NOW(), NOW()),
(5, 'Senior Citizen Special', 'Discounted service for over 65s', 18.00, 25, NOW(), NOW());

-- Shop 6: Ranelagh Razor
INSERT INTO services (shop_id, name, description, price, duration_minutes, created_at, updated_at) VALUES
(6, 'Ranelagh Special', 'Trendy cut with craft beer included', 35.00, 40, NOW(), NOW()),
(6, 'Drop Fade', 'Contemporary drop fade styling', 38.00, 40, NOW(), NOW()),
(6, 'Beard Design', 'Creative beard shaping and line work', 28.00, 25, NOW(), NOW()),
(6, 'Long Hair Cut', 'Scissor work for longer styles', 40.00, 50, NOW(), NOW()),
(6, 'Full Grooming Experience', 'Cut, beard, and beverage', 60.00, 70, NOW(), NOW());

-- Shop 7: Phizzers Barbershop
INSERT INTO services (shop_id, name, description, price, duration_minutes, created_at, updated_at) VALUES
(7, 'Walk-In Cut', 'No appointment needed, great value', 24.00, 25, NOW(), NOW()),
(7, 'Student Cut', 'Affordable cut with student ID', 20.00, 25, NOW(), NOW()),
(7, 'Fade Haircut', 'Classic fade styling', 28.00, 30, NOW(), NOW()),
(7, 'Beard Shaping', 'Basic beard maintenance', 16.00, 15, NOW(), NOW()),
(7, 'Cut & Beard Deal', 'Value package', 38.00, 45, NOW(), NOW());

-- Shop 8: Blackrock Barber House
INSERT INTO services (shop_id, name, description, price, duration_minutes, created_at, updated_at) VALUES
(8, 'Coastal Cut', 'Relaxed haircut with sea views', 32.00, 35, NOW(), NOW()),
(8, 'Precision Fade', 'Detailed fade work', 36.00, 40, NOW(), NOW()),
(8, 'Beard & Mustache Trim', 'Full facial hair grooming', 22.00, 20, NOW(), NOW()),
(8, 'Traditional Shave', 'Hot towel wet shave', 38.00, 40, NOW(), NOW()),
(8, 'Premium Package', 'Full service grooming', 65.00, 75, NOW(), NOW());

-- Shop 9: Stoneybatter Style House
INSERT INTO services (shop_id, name, description, price, duration_minutes, created_at, updated_at) VALUES
(9, 'Vintage Cut', 'Classic styles with modern finish', 33.00, 35, NOW(), NOW()),
(9, 'Undercut Fade', 'Trendy undercut with fade sides', 37.00, 40, NOW(), NOW()),
(9, 'Beard Art', 'Creative beard designs and patterns', 26.00, 25, NOW(), NOW()),
(9, 'Buzz & Fade', 'Short all-over with faded sides', 28.00, 30, NOW(), NOW()),
(9, 'Style House Special', 'Cut and beard with vinyl listening session', 55.00, 65, NOW(), NOW());

-- Shop 10: Portobello Parlour
INSERT INTO services (shop_id, name, description, price, duration_minutes, created_at, updated_at) VALUES
(10, 'Canal Cut', 'Precision haircut with complimentary espresso', 34.00, 35, NOW(), NOW()),
(10, 'Shadow Fade', 'Subtle fade with natural blending', 38.00, 40, NOW(), NOW()),
(10, 'Beard Refinement', 'Detailed beard work and oils', 24.00, 20, NOW(), NOW()),
(10, 'Scissor Over Comb', 'Traditional cutting technique', 32.00, 35, NOW(), NOW()),
(10, 'Parlour Premium', 'Full grooming with coffee ritual', 68.00, 70, NOW(), NOW());

-- Shop 11: Donnybrook Distinguished Barbers
INSERT INTO services (shop_id, name, description, price, duration_minutes, created_at, updated_at) VALUES
(11, 'Distinguished Cut', 'Sophisticated styling for professionals', 42.00, 40, NOW(), NOW()),
(11, 'Executive Shave', 'Luxury wet shave with premium products', 48.00, 45, NOW(), NOW()),
(11, 'Beard Consultation', 'Expert beard advice and shaping', 35.00, 30, NOW(), NOW()),
(11, 'Silver Fox Style', 'Specialized cut for grey hair', 45.00, 45, NOW(), NOW()),
(11, 'Members Package', 'Exclusive full service', 90.00, 90, NOW(), NOW());

-- Shop 12: Smithfield Square Barbers
INSERT INTO services (shop_id, name, description, price, duration_minutes, created_at, updated_at) VALUES
(12, 'Square Cut', 'Modern styling in historic setting', 31.00, 35, NOW(), NOW()),
(12, 'Bald Fade', 'Clean fade to bare skin', 35.00, 40, NOW(), NOW()),
(12, 'Goatee Sculpting', 'Specialized goatee styling', 20.00, 20, NOW(), NOW()),
(12, 'Long Hair Trim', 'Maintenance cut for longer styles', 36.00, 45, NOW(), NOW()),
(12, 'Smithfield Special', 'Cut, beard, and head massage', 58.00, 65, NOW(), NOW());

-- Shop 13: Sandymount Shore Barbers
INSERT INTO services (shop_id, name, description, price, duration_minutes, created_at, updated_at) VALUES
(13, 'Shore Cut', 'Relaxed beach-vibe haircut', 28.00, 30, NOW(), NOW()),
(13, 'Family Fade', 'Fade cut for all ages', 32.00, 35, NOW(), NOW()),
(13, 'Beard Basics', 'Simple beard trim and shape', 18.00, 15, NOW(), NOW()),
(13, 'Kids Special', 'Fun cut for little ones', 20.00, 20, NOW(), NOW()),
(13, 'Shore Package', 'Cut and beard combo', 42.00, 50, NOW(), NOW());

-- Shop 14: Harold's Cross Hair Co.
INSERT INTO services (shop_id, name, description, price, duration_minutes, created_at, updated_at) VALUES
(14, 'Traditional Cut', 'Three generations of expertise', 26.00, 30, NOW(), NOW()),
(14, 'Modern Fade', 'Classic shop, modern techniques', 30.00, 35, NOW(), NOW()),
(14, 'Full Beard Service', 'Complete beard grooming', 22.00, 20, NOW(), NOW()),
(14, 'Quick Trim', 'Fast tidy-up cut', 20.00, 20, NOW(), NOW()),
(14, 'Heritage Package', 'Traditional full service', 45.00, 60, NOW(), NOW());

-- Shop 15: Clontarf Clippers
INSERT INTO services (shop_id, name, description, price, duration_minutes, created_at, updated_at) VALUES
(15, 'Clippers Special', 'Seaside haircut with panoramic views', 30.00, 30, NOW(), NOW()),
(15, 'Coastal Fade', 'Fresh fade with sea breeze', 34.00, 35, NOW(), NOW()),
(15, 'Beard Taming', 'Control and style unruly beards', 20.00, 20, NOW(), NOW()),
(15, 'Clipper Over Comb', 'Precision clipper technique', 32.00, 35, NOW(), NOW()),
(15, 'North Coast Package', 'Full grooming experience', 55.00, 65, NOW(), NOW());


-- ========================================
-- EMPLOYEES (3-4 per shop = ~50 total)
-- ========================================

-- Shop 1: The Grafton Barber
INSERT INTO employees (shop_id, name, role, email, phone, created_at, updated_at) VALUES
(1, 'Conor McDonagh', 'Senior Barber', 'conor@graftonbarber.ie', '+353 87 601 0001', NOW(), NOW()),
(1, 'Sarah O''Reilly', 'Master Barber', 'sarah@graftonbarber.ie', '+353 87 601 0002', NOW(), NOW()),
(1, 'Michael Flynn', 'Barber', 'michael@graftonbarber.ie', '+353 87 601 0003', NOW(), NOW());

-- Shop 2: Northside Cuts
INSERT INTO employees (shop_id, name, role, email, phone, created_at, updated_at) VALUES
(2, 'David Kavanagh', 'Head Barber', 'david@northsidecuts.ie', '+353 87 602 0001', NOW(), NOW()),
(2, 'Emma Collins', 'Barber', 'emma@northsidecuts.ie', '+353 87 602 0002', NOW(), NOW()),
(2, 'Luke Brennan', 'Junior Barber', 'luke@northsidecuts.ie', '+353 87 602 0003', NOW(), NOW()),
(2, 'Rachel Murphy', 'Barber', 'rachel@northsidecuts.ie', '+353 87 602 0004', NOW(), NOW());

-- Shop 3: Rathmines Barber Co.
INSERT INTO employees (shop_id, name, role, email, phone, created_at, updated_at) VALUES
(3, 'Paul Doherty', 'Master Barber', 'paul@rathminesbarber.ie', '+353 87 603 0001', NOW(), NOW()),
(3, 'Katie Walsh', 'Senior Barber', 'katie@rathminesbarber.ie', '+353 87 603 0002', NOW(), NOW()),
(3, 'James Ryan', 'Barber', 'james@rathminesbarber.ie', '+353 87 603 0003', NOW(), NOW());

-- Shop 4: Ballsbridge Barbers
INSERT INTO employees (shop_id, name, role, email, phone, created_at, updated_at) VALUES
(4, 'Patrick Higgins', 'Senior Barber', 'patrick@ballsbridgebarbers.ie', '+353 87 604 0001', NOW(), NOW()),
(4, 'Aoife Kennedy', 'Master Barber', 'aoife@ballsbridgebarbers.ie', '+353 87 604 0002', NOW(), NOW()),
(4, 'Brian O''Connor', 'Barber', 'brian@ballsbridgebarbers.ie', '+353 87 604 0003', NOW(), NOW());

-- Shop 5: Drumcondra Grooming
INSERT INTO employees (shop_id, name, role, email, phone, created_at, updated_at) VALUES
(5, 'Tom Gallagher', 'Head Barber', 'tom@drumcondragrooming.ie', '+353 87 605 0001', NOW(), NOW()),
(5, 'Niamh Byrne', 'Barber', 'niamh@drumcondragrooming.ie', '+353 87 605 0002', NOW(), NOW()),
(5, 'Sean Doyle', 'Junior Barber', 'sean@drumcondragrooming.ie', '+353 87 605 0003', NOW(), NOW()),
(5, 'Claire O''Brien', 'Barber', 'claire@drumcondragrooming.ie', '+353 87 605 0004', NOW(), NOW());

-- Shop 6: Ranelagh Razor
INSERT INTO employees (shop_id, name, role, email, phone, created_at, updated_at) VALUES
(6, 'Dylan McCarthy', 'Master Barber', 'dylan@ranelaghrazor.ie', '+353 87 606 0001', NOW(), NOW()),
(6, 'Sinead Quinn', 'Senior Barber', 'sinead@ranelaghrazor.ie', '+353 87 606 0002', NOW(), NOW()),
(6, 'Mark Kelly', 'Barber', 'mark@ranelaghrazor.ie', '+353 87 606 0003', NOW(), NOW());

-- Shop 7: Phizzers Barbershop
INSERT INTO employees (shop_id, name, role, email, phone, created_at, updated_at) VALUES
(7, 'Gary Lynch', 'Head Barber', 'gary@phizzers.ie', '+353 87 607 0001', NOW(), NOW()),
(7, 'Lisa Murray', 'Barber', 'lisa@phizzers.ie', '+353 87 607 0002', NOW(), NOW()),
(7, 'Adam Brennan', 'Barber', 'adam@phizzers.ie', '+353 87 607 0003', NOW(), NOW()),
(7, 'Sophie Dunne', 'Junior Barber', 'sophie@phizzers.ie', '+353 87 607 0004', NOW(), NOW());

-- Shop 8: Blackrock Barber House
INSERT INTO employees (shop_id, name, role, email, phone, created_at, updated_at) VALUES
(8, 'Robert Kennedy', 'Senior Barber', 'robert@blackrockbarber.ie', '+353 87 608 0001', NOW(), NOW()),
(8, 'Orla Quinn', 'Master Barber', 'orla@blackrockbarber.ie', '+353 87 608 0002', NOW(), NOW()),
(8, 'Eoin Maguire', 'Barber', 'eoin@blackrockbarber.ie', '+353 87 608 0003', NOW(), NOW());

-- Shop 9: Stoneybatter Style House
INSERT INTO employees (shop_id, name, role, email, phone, created_at, updated_at) VALUES
(9, 'Jake O''Donnell', 'Master Barber', 'jake@stoneybatterstyle.ie', '+353 87 609 0001', NOW(), NOW()),
(9, 'Anna Fitzgerald', 'Senior Barber', 'anna@stoneybatterstyle.ie', '+353 87 609 0002', NOW(), NOW()),
(9, 'Chris Martin', 'Barber', 'chris@stoneybatterstyle.ie', '+353 87 609 0003', NOW(), NOW());

-- Shop 10: Portobello Parlour
INSERT INTO employees (shop_id, name, role, email, phone, created_at, updated_at) VALUES
(10, 'Daniel Clarke', 'Senior Barber', 'daniel@portobelloparlour.ie', '+353 87 610 0001', NOW(), NOW()),
(10, 'Melissa Evans', 'Master Barber', 'melissa@portobelloparlour.ie', '+353 87 610 0002', NOW(), NOW()),
(10, 'Kevin Moore', 'Barber', 'kevin@portobelloparlour.ie', '+353 87 610 0003', NOW(), NOW()),
(10, 'Laura Walker', 'Barber', 'laura@portobelloparlour.ie', '+353 87 610 0004', NOW(), NOW());

-- Shop 11: Donnybrook Distinguished Barbers
INSERT INTO employees (shop_id, name, role, email, phone, created_at, updated_at) VALUES
(11, 'William Robinson', 'Master Barber', 'william@donnybrookbarbers.ie', '+353 87 611 0001', NOW(), NOW()),
(11, 'Grace King', 'Senior Barber', 'grace@donnybrookbarbers.ie', '+353 87 611 0002', NOW(), NOW()),
(11, 'Henry Baker', 'Barber', 'henry@donnybrookbarbers.ie', '+353 87 611 0003', NOW(), NOW());

-- Shop 12: Smithfield Square Barbers
INSERT INTO employees (shop_id, name, role, email, phone, created_at, updated_at) VALUES
(12, 'Ryan Green', 'Head Barber', 'ryan@smithfieldbarbers.ie', '+353 87 612 0001', NOW(), NOW()),
(12, 'Amy Wright', 'Senior Barber', 'amy@smithfieldbarbers.ie', '+353 87 612 0002', NOW(), NOW()),
(12, 'Nathan Hill', 'Barber', 'nathan@smithfieldbarbers.ie', '+353 87 612 0003', NOW(), NOW()),
(12, 'Zoe Scott', 'Junior Barber', 'zoe@smithfieldbarbers.ie', '+353 87 612 0004', NOW(), NOW());

-- Shop 13: Sandymount Shore Barbers
INSERT INTO employees (shop_id, name, role, email, phone, created_at, updated_at) VALUES
(13, 'Peter Adams', 'Head Barber', 'peter@sandymountbarbers.ie', '+353 87 613 0001', NOW(), NOW()),
(13, 'Jennifer Cox', 'Senior Barber', 'jennifer@sandymountbarbers.ie', '+353 87 613 0002', NOW(), NOW()),
(13, 'Andrew Bailey', 'Barber', 'andrew@sandymountbarbers.ie', '+353 87 613 0003', NOW(), NOW());

-- Shop 14: Harold's Cross Hair Co.
INSERT INTO employees (shop_id, name, role, email, phone, created_at, updated_at) VALUES
(14, 'Martin Foster', 'Master Barber', 'martin@haroldscross.ie', '+353 87 614 0001', NOW(), NOW()),
(14, 'Fiona Gray', 'Senior Barber', 'fiona@haroldscross.ie', '+353 87 614 0002', NOW(), NOW()),
(14, 'Colin Howard', 'Barber', 'colin@haroldscross.ie', '+353 87 614 0003', NOW(), NOW()),
(14, 'Rebecca Ward', 'Barber', 'rebecca@haroldscross.ie', '+353 87 614 0004', NOW(), NOW());

-- Shop 15: Clontarf Clippers
INSERT INTO employees (shop_id, name, role, email, phone, created_at, updated_at) VALUES
(15, 'Stephen Mitchell', 'Senior Barber', 'stephen@clontarfclippers.ie', '+353 87 615 0001', NOW(), NOW()),
(15, 'Caroline Perry', 'Master Barber', 'caroline@clontarfclippers.ie', '+353 87 615 0002', NOW(), NOW()),
(15, 'Alex Turner', 'Barber', 'alex@clontarfclippers.ie', '+353 87 615 0003', NOW(), NOW());
