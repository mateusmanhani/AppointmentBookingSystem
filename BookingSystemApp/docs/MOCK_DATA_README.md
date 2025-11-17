# Mock Data Seed Files - Dublin Barbershop Booking System

## Overview
Comprehensive seed data for presentation and demo purposes, featuring 15 authentic Dublin barbershops with real GPS coordinates, complete services, employees, and sample appointments.

## Data Structure

### 👥 Users (`user-service/data.sql`)
**Total: 35 users**
- **Shop Owners (15)**: IDs 1-15
  - Irish names (Liam Murphy, Sean Kelly, Connor Walsh, etc.)
  - Email format: `owner{N}@barbershop.ie`
  - Phone: `+353 1 234 500{N}`
  
- **Customers (20)**: IDs 16-35
  - Mix of Irish and international names
  - Email format: `{firstname}.{lastname}@email.ie`
  - Phone: `+353 87 123 45{NN}`

**Default Password for All Users**: `Egmqr123k*`
**BCrypt Hash**: `$2a$12$aOvmhzdxSx1/9ahB9CndpuWS7PLOYYnf6ZPiPlc14GsutlgucUCWm`

---

### 🏪 Shops (`shop-service/data.sql`)
**Total: 15 Dublin shops with authentic locations**

| ID | Shop Name | Area | Address | Coordinates |
|----|-----------|------|---------|-------------|
| 1 | The Grafton Barber | Temple Bar | 45 South William Street | 53.342778, -6.264167 |
| 2 | Northside Cuts | O'Connell St | 28 O'Connell Street Upper | 53.351389, -6.260556 |
| 3 | Rathmines Barber Co. | Rathmines | 156 Rathmines Road Lower | 53.325000, -6.265833 |
| 4 | Ballsbridge Barbers | Ballsbridge | 42 Pembroke Road | 53.330556, -6.237778 |
| 5 | Drumcondra Grooming | Drumcondra | 67 Drumcondra Road Lower | 53.368333, -6.256667 |
| 6 | Ranelagh Razor | Ranelagh | 12 Ranelagh Village | 53.322222, -6.253889 |
| 7 | Phizzers Barbershop | Phibsborough | 34 Phibsborough Road | 53.360833, -6.273333 |
| 8 | Blackrock Barber House | Blackrock | 78 Main Street Blackrock | 53.301111, -6.177778 |
| 9 | Stoneybatter Style House | Stoneybatter | 23 Manor Street | 53.354167, -6.284444 |
| 10 | Portobello Parlour | Portobello | 67 South Circular Road | 53.334722, -6.269167 |
| 11 | Donnybrook Distinguished | Donnybrook | 12 Donnybrook Road | 53.317222, -6.233611 |
| 12 | Smithfield Square Barbers | Smithfield | 5 Smithfield Square | 53.347500, -6.278611 |
| 13 | Sandymount Shore Barbers | Sandymount | 45 Sandymount Road | 53.330000, -6.218056 |
| 14 | Harold's Cross Hair Co. | Harold's Cross | 89 Harold's Cross Road | 53.323333, -6.281111 |
| 15 | Clontarf Clippers | Clontarf | 156 Clontarf Road | 53.363889, -6.216667 |

**Operating Hours**: Mix of 08:00-20:00 (most common: 09:00-18:30/19:00)

---

### ✂️ Services (`shop-service/data.sql`)
**Total: 75 services (5 per shop)**

**Common Services per Shop**:
1. **Basic/Classic Cut** (€20-€45) - 25-45 minutes
2. **Fade/Modern Cut** (€28-€38) - 30-40 minutes
3. **Beard Services** (€15-€30) - 15-30 minutes
4. **Traditional Shave** (€38-€50) - 40-50 minutes
5. **Package/Combo** (€40-€95) - 50-90 minutes

**Price Range**: €15 (basic beard trim) to €95 (platinum package)
**Duration Range**: 15 minutes to 90 minutes

**Example Service Themes**:
- Temple Bar (Shop 1): Premium services, hot towel shaves
- Northside (Shop 2): Family-friendly, kids cuts, father-son combos
- Ballsbridge (Shop 4): Luxury executive services
- Ranelagh (Shop 6): Trendy with craft beer included

---

### 👨‍💼 Employees (`shop-service/data.sql`)
**Total: 50 employees (3-4 per shop)**

**Roles**:
- Master Barber
- Senior Barber
- Head Barber
- Barber
- Junior Barber

**Irish Names Sample**: Conor McDonagh, Sarah O'Reilly, Katie Walsh, Dylan McCarthy, Aoife Kennedy, etc.

**Contact**: Each employee has email (`{name}@{shopname}.ie`) and phone (`+353 87 60{X} 000{Y}`)

---

### 📅 Appointments (`appointment-service/data.sql`)
**Total: ~75 appointments with realistic distribution**

#### **Status Distribution**:
- **COMPLETED** (~20): Past appointments (1-14 days ago)
- **CANCELLED** (~5): Past week, various reasons
- **CONFIRMED** (~30): Today through next 2 weeks
- **PENDING** (~20): Awaiting confirmation, future appointments

#### **Temporal Distribution**:
- **Past (COMPLETED)**: 2 weeks ago → yesterday
- **Today**: 4 confirmed appointments across different shops
- **Tomorrow**: 5 appointments (mix of confirmed/pending)
- **2-7 Days Ahead**: ~25 appointments (peak booking period)
- **2-4 Weeks Ahead**: ~15 appointments (advance bookings)

#### **Notes Sample**:
- "Great service, very professional"
- "My son loved the cut!"
- "Had to cancel, family emergency"
- "Looking forward to full service package"
- "Will have craft beer"

---

## 🗺️ Geographic Coverage

### Dublin City Center
- Temple Bar (Shop 1): Heart of creative quarter
- O'Connell Street (Shop 2): Main thoroughfare
- Smithfield (Shop 12): Historic revitalized area

### South Dublin
- Rathmines (Shop 3): Student area
- Ballsbridge (Shop 4): Affluent D4
- Ranelagh (Shop 6): Trendy village
- Portobello (Shop 10): Canal area
- Donnybrook (Shop 11): Upscale residential

### North Dublin
- Drumcondra (Shop 5): Near Croke Park
- Phibsborough (Shop 7): Community area
- Stoneybatter (Shop 9): Hip neighborhood

### Coastal
- Blackrock (Shop 8): South coast
- Sandymount (Shop 13): Seafront
- Clontarf (Shop 15): North coast

### Other
- Harold's Cross (Shop 14): Traditional area

---

## 🚀 Usage

### Automatic Loading
Data files are configured to load automatically with dynamic dates based on `CURDATE()`:

```yaml
spring:
  sql:
    init:
      mode: always  # Data resets on every restart - useful for testing
      continue-on-error: false
```

**Benefits of Dynamic Loading**:
- Dates are always relative to current date (past appointments stay in the past)
- No stale data issues
- Perfect for demo/testing scenarios
- Fresh data on every service restart

### Manual Reset (if needed)
Data automatically resets on service restart with `mode: always`. 

To disable auto-reset (keep data persistent):
1. Change `spring.sql.init.mode` from `always` to `never` in all 3 config files
2. Data will persist across restarts

To manually clear data via SQL:

```bash
# Stop all services first

# MySQL commands
mysql -u barbershop_user -p

USE user_service_db;
DELETE FROM users WHERE id > 0;
ALTER TABLE users AUTO_INCREMENT = 1;

USE shop_service_db;
DELETE FROM services WHERE id > 0;
DELETE FROM employees WHERE id > 0;
DELETE FROM shops WHERE id > 0;
ALTER TABLE shops AUTO_INCREMENT = 1;

USE appointment_service_db;
DELETE FROM appointments WHERE id > 0;
ALTER TABLE appointments AUTO_INCREMENT = 1;

# Then restart services - data.sql will reload automatically
```

---

## 🎯 Demo Scenarios

### 1. **Customer Booking Flow**
- Login as customer (e.g., `james.wilson@email.ie` / `password123`)
- Browse shops across Dublin
- Select shop → view services → book appointment
- View dashboard with past/upcoming appointments

### 2. **Shop Owner Dashboard** (Future Feature)
- Login as shop owner (e.g., `owner1@barbershop.ie` / `password123`)
- View day's appointments for "The Grafton Barber"
- Filter by employee (Conor, Sarah, Michael)
- See booking patterns and availability

### 3. **Real-Time Availability Map** (Sprint 4)
- View map centered on Dublin city center (53.349805, -6.260310)
- See 15 barbershop markers across Dublin
- Click shop → view availability heatmap
- Color-coded by available slots:
  - 🟢 Green: High availability (8+ slots)
  - 🟡 Yellow: Moderate availability (3-7 slots)
  - 🔴 Red: Low availability (1-2 slots)
  - ⚫ Grey: Fully booked

### 4. **Appointment Management**
- View all appointments in various states
- Cancel future appointments
- Reschedule with conflict detection
- Edit mode with employee-specific availability

---

## 📊 Data Quality Features

✅ **Realistic Irish Context**
- Authentic Dublin addresses and postal codes
- Irish phone format (+353)
- Local neighborhood character and descriptions
- Irish names for employees and owners

✅ **Referential Integrity**
- All foreign keys validated
- Owner IDs match user IDs
- Employee IDs belong to correct shops
- Service IDs match shop associations

✅ **Temporal Realism**
- Mix of past/present/future appointments
- Realistic booking patterns (more near-term, fewer far-term)
- Business hours respect Irish standards
- Date ranges use SQL functions (CURDATE(), DATE_ADD())

✅ **Business Logic**
- No double-booking conflicts
- Varied service prices and durations
- Status transitions make sense (COMPLETED for past, PENDING for future)
- Employee assignments varied across appointments

---

## 🛠️ Maintenance

### Adding More Shops
1. Add shop owner to `user-service/data.sql` (next available ID)
2. Add shop with real Dublin coordinates to `shop-service/data.sql`
3. Add 3-5 services for the shop
4. Add 3-4 employees for the shop
5. Optionally add sample appointments

### Updating Coordinates
GPS coordinates sourced from Google Maps. To verify:
```
https://www.google.com/maps/search/{address}
```
Right-click location → "What's here?" → Copy coordinates

### Modifying Appointment Dates
Dates use SQL date functions relative to current date:
- `CURDATE()` - Today
- `DATE_SUB(CURDATE(), INTERVAL X DAY)` - X days ago
- `DATE_ADD(CURDATE(), INTERVAL X DAY)` - X days from now

This ensures data stays relevant regardless of when services start.

---

## 📝 Notes

- **Character Encoding**: SQL files use UTF-8 for Irish names (O'Brien, O'Sullivan, etc.)
- **SQL Dialect**: MySQL-compatible (AUTO_INCREMENT, NOW(), DATE functions)
- **Service IDs**: Sequential by shop (Shop 1: 1-5, Shop 2: 6-10, etc.)
- **Employee IDs**: Sequential across all shops (1-50)
- **Test Account**: Use `james.wilson@email.ie` / `Egmqr123k*` for customer testing
- **Dynamic Dates**: All appointment dates use `CURDATE()`, `DATE_ADD()`, `DATE_SUB()` for relative dates
- **Auto-Reset**: With `mode: always`, data reloads fresh on every restart (great for testing!)

---

## 🎓 Presentation Tips

1. **Start with Map View**: Show all 15 Dublin shops plotted geographically
2. **Zoom into Area**: Focus on Temple Bar/City Center cluster
3. **Select Premium Shop**: "The Grafton Barber" for upscale demo
4. **Show Service Range**: €30-€75 services with detailed descriptions
5. **Demonstrate Booking**: Full flow with employee selection
6. **Display Dashboard**: Show mix of past (completed), today (confirmed), upcoming (pending)
7. **Test Reschedule**: Edit an existing appointment with conflict detection
8. **Owner View** (if implemented): Show daily schedule for a shop

---

**Created**: January 2025  
**Purpose**: NCI Final Project Presentation  
**Maintained By**: Development Team
