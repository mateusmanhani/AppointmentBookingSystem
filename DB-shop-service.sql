-- Create the database
CREATE DATABASE shop_service_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Grant privilleges to user already created
GRANT ALL PRIVILEGES ON shop_service_db.* TO 'barbershop_user'@'localhost';
FLUSH PRIVILEGES;

-- Verify database creation
SHOW DATABASES;
USE shop_service_db;