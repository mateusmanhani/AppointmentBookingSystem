-- Create the database
CREATE DATABASE user_service_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Create a dedicated user for the application (recommended for security)
CREATE USER 'barbershop_user'@'localhost' IDENTIFIED BY 'barbershop_pass123';
GRANT ALL PRIVILEGES ON user_service_db.* TO 'barbershop_user'@'localhost';
FLUSH PRIVILEGES;

-- Verify database creation
SHOW DATABASES;
USE user_service_db;