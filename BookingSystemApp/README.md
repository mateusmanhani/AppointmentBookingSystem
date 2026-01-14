# Barbershop Booking System

A modern microservices-based appointment booking system for barbershops built with Spring Boot 3.5.6 and Java 21.

## 📋 Table of Contents
- [Overview](#overview)
- [Architecture](#architecture)
- [Technologies](#technologies)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [Running the Application](#running-the-application)
- [API Endpoints](#api-endpoints)
- [Default Ports](#default-ports)

## 🎯 Overview

This booking system allows customers to:
- Register and manage their accounts
- Browse available barbershops
- View available time slots
- Book appointments with barbers
- Manage their bookings

Barbers and shop owners can:
- Manage shop information
- Set availability schedules
- View and manage appointments

## 🏗️ Architecture

The system follows a microservices architecture with the following services:

### 1. **User Service** (Port 8081)
- User registration and authentication
- JWT token generation and validation
- User profile management
- Role-based access control (Customer, Barber, Shop Owner, Admin)

### 2. **Shop Service** (Port 8082)
- Barbershop information management
- Service offerings (haircut, shave, beard trim, etc.)
- Shop hours and location details

### 3. **Appointment Service** (Port 8083)
- Appointment booking and management
- Availability checking
- Time slot management
- Appointment status tracking (Pending, Confirmed, Completed, Cancelled)

### 4. **Frontend Application** (Port 8080)
- Web-based user interface
- Static HTML pages with modern CSS styling
- Interactive booking forms
- Dashboard for managing appointments

## 🛠️ Technologies

- **Java 21** - Modern Java features and virtual threads
- **Spring Boot 3.5.6** - Application framework
- **Spring Security** - Authentication and authorization
- **Spring Data JPA** - Database interaction
- **OAuth2 Resource Server** - JWT token validation
- **MySQL 8.0.33** - Database
- **Maven** - Build and dependency management
- **Thymeleaf** - Template engine for frontend

## ✅ Prerequisites

Before running the application, ensure you have:

- **Java 21** installed ([Download here](https://adoptium.net/))
- **Maven 3.8+** installed
- **MySQL 8.0+** running on localhost
- A MySQL user with permissions to create databases

## 🚀 Getting Started

### 1. Clone the Repository
```bash
git clone <repository-url>
cd BookingSystemApp
```

### 2. Set Up MySQL Databases

Connect to MySQL and create the required databases:

```sql
-- Create databases
CREATE DATABASE user_service_db;
CREATE DATABASE shop_service_db;
CREATE DATABASE appointment_service_db;

-- Create user and grant privileges
CREATE USER IF NOT EXISTS 'barbershop_user'@'localhost' IDENTIFIED BY 'barbershop_pass123';
GRANT ALL PRIVILEGES ON user_service_db.* TO 'barbershop_user'@'localhost';
GRANT ALL PRIVILEGES ON shop_service_db.* TO 'barbershop_user'@'localhost';
GRANT ALL PRIVILEGES ON appointment_service_db.* TO 'barbershop_user'@'localhost';
FLUSH PRIVILEGES;
```

### 3. Build the Project

From the root directory:

```bash
mvn clean install
```

This will build all microservices.

## ▶️ Running the Application

### Option 1: Run All Services Individually

Open **4 separate terminals** and run each service:

**Terminal 1 - User Service:**
```bash
cd user-service
mvn spring-boot:run
```

**Terminal 2 - Shop Service:**
```bash
cd shop-service
mvn spring-boot:run
```

**Terminal 3 - Appointment Service:**
```bash
cd appointment-service
mvn spring-boot:run
```

**Terminal 4 - Frontend Application:**
```bash
cd FrontendApplication
mvn spring-boot:run
```

### Option 2: Run Pre-built JARs

After building, you can run the JAR files:

```bash
# Terminal 1
java -jar user-service/target/user-service-1.0.0.jar

# Terminal 2
java -jar shop-service/target/shop-service-1.0.0.jar

# Terminal 3
java -jar appointment-service/target/appointment-service-1.0.0.jar

# Terminal 4
java -jar FrontendApplication/target/FrontendApplication-0.0.1-SNAPSHOT.jar
```

### ✅ Verify Services Are Running

Check that all services are up:
- User Service: http://localhost:8081/actuator/health
- Shop Service: http://localhost:8082/actuator/health
- Appointment Service: http://localhost:8083/actuator/health
- Frontend: http://localhost:8080

## 🌐 Accessing the Application

Once all services are running, open your browser and navigate to:

```
http://localhost:8080
```

You'll see the main page where you can:
- Register a new account
- Login with existing credentials
- Browse shops and services
- Book appointments

## 🔐 API Endpoints

### User Service (Port 8081)

```
POST   /api/auth/register          - Register new user
POST   /api/auth/login             - Login and get JWT token
POST   /api/auth/refresh           - Refresh JWT token
GET    /api/users                  - Get all users (Admin only)
GET    /api/users/{id}             - Get user by ID
PUT    /api/users/{id}             - Update user
DELETE /api/users/{id}             - Delete user
```

### Shop Service (Port 8082)

```
GET    /api/shops                  - Get all shops
GET    /api/shops/{id}             - Get shop by ID
POST   /api/shops                  - Create new shop (Shop Owner)
PUT    /api/shops/{id}             - Update shop
DELETE /api/shops/{id}             - Delete shop
GET    /api/shops/{id}/services    - Get services offered by shop
```

### Appointment Service (Port 8083)

```
GET    /api/appointments                     - Get all appointments
GET    /api/appointments/{id}                - Get appointment by ID
POST   /api/appointments                     - Create new appointment
PUT    /api/appointments/{id}                - Update appointment
DELETE /api/appointments/{id}                - Cancel appointment
GET    /api/appointments/user/{userId}       - Get user's appointments
GET    /api/appointments/shop/{shopId}       - Get shop's appointments
GET    /api/availability/shop/{shopId}       - Check availability for shop
```

## 🔑 Default Ports

| Service | Port |
|---------|------|
| Frontend Application | 8080 |
| User Service | 8081 |
| Shop Service | 8082 |
| Appointment Service | 8083 |

## 📝 Default Credentials

The application comes with sample data. You can use these credentials to test:

### Admin
- Email: `admin@barbershop.com`
- Password: `admin123`

### Shop Owner
- Email: `owner@barbershop.com`
- Password: `owner123`

### Barber
- Email: `barber@barbershop.com`
- Password: `barber123`

### Customer
- Email: `customer@barbershop.com`
- Password: `customer123`

> **Note:** Credentials may vary based on your data.sql initialization scripts.

## 🗂️ Project Structure

```
BookingSystemApp/
├── pom.xml                      # Parent POM
├── user-service/                # User management service
│   ├── src/
│   └── pom.xml
├── shop-service/                # Shop management service
│   ├── src/
│   └── pom.xml
├── appointment-service/         # Appointment booking service
│   ├── src/
│   └── pom.xml
└── FrontendApplication/         # Web UI
    ├── src/
    │   └── main/
    │       └── resources/
    │           └── static/      # HTML, CSS, JS files
    └── pom.xml
```

## 🔒 Security

- All services use JWT tokens for authentication
- Passwords are encrypted using BCrypt
- CORS is configured to allow frontend communication
- OAuth2 resource server validates JWT tokens

## 🐛 Troubleshooting

### Database Connection Issues
- Verify MySQL is running: `mysql -u root -p`
- Check database credentials in `application.yml` files
- Ensure databases are created

### Port Already in Use
If a port is already occupied, you can change it in the respective `application.yml` file:
```yaml
server:
  port: 8081  # Change to available port
```

### Build Failures
- Ensure Java 21 is installed: `java -version`
- Clear Maven cache: `mvn clean`
- Update dependencies: `mvn clean install -U`

## 📄 License

This project is created for educational purposes.

## 👥 Contributors

NCI Final Project Team

---

**For more information or support, please contact the development team.**
