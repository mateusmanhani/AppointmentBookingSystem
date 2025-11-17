# 🚀 Quick Start: Loading Mock Data (Dynamic Mode)

## Overview
The system is configured for **dynamic data loading** where fresh data is loaded on every service restart. This ensures:
- ✅ Dates are always current (relative to today)
- ✅ No stale data issues
- ✅ Perfect for testing and demos
- ✅ Appointments appear in correct past/present/future states

## Default Configuration

All services are set to `spring.sql.init.mode: always` which means:
- Data is **deleted and recreated** on every restart
- Dates are dynamically calculated from `CURDATE()`
- Fresh demo data every time

## First Time Setup

### Step 1: Verify Configuration (Already Set)
All 3 services should already have `mode: always` configured:

**1. `user-service/src/main/resources/application-local.yml`**
```yaml
spring:
  sql:
    init:
      mode: always  # ✅ Already configured for dynamic loading
```

**2. `shop-service/src/main/resources/application.yml`**
```yaml
spring:
  sql:
    init:
      mode: always  # ✅ Already configured for dynamic loading
```

**3. `appointment-service/src/main/resources/application.yaml`**
```yaml
spring:
  sql:
    init:
      mode: always  # ✅ Already configured for dynamic loading
```

### Step 2: Start Services
```powershell
# Start each service (or use your IDE)
cd user-service
mvn spring-boot:run

cd ../shop-service
mvn spring-boot:run

cd ../appointment-service
mvn spring-boot:run
```

### Step 3: Verify Data Loaded
Check the console logs for:
```
Executing SQL script from URL [file:/.../data.sql]
```

Or test manually:
```sql
USE user_service_db;
SELECT COUNT(*) FROM users;  -- Should return 35

USE shop_service_db;
SELECT COUNT(*) FROM shops;  -- Should return 15

USE appointment_service_db;
SELECT COUNT(*) FROM appointments;  -- Should return ~75
```

### Step 4: Test Login
**Username**: `james.wilson@email.ie`  
**Password**: `Egmqr123k*`

All users (shop owners and customers) use the same password: `Egmqr123k*`

---

## Subsequent Restarts

With `mode: always` (default), services will **automatically reload fresh data** on every restart.

**This means**:
- ✅ All appointments have current dates (relative to today)
- ✅ No accumulation of old test data
- ✅ Consistent demo experience every time
- ✅ Perfect for testing with time-sensitive features

**Note**: Any manual data changes (bookings, user registrations) will be **lost on restart**. This is intentional for clean testing.

---

## Making Data Persistent (Optional)

If you want data to **persist across restarts** (for development):

1. Change `mode: always` → `mode: never` in all 3 config files
2. Restart services **once** to load the initial data
3. Data now persists in MySQL
4. Manual bookings and changes are kept across restarts

**When to use persistent mode**:
- Long-term development testing
- You need to keep specific booking scenarios
- Testing user flows across multiple sessions

**When to use dynamic mode (current)**:
- Demos and presentations
- Testing with current dates
- Clean slate for each test session

---

## Resetting Data

### Quick Reset (Current Mode)
Just restart the services - data auto-reloads! 🎉

### Manual SQL Reset (If needed)
```sql
-- Clear all data
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

-- Then restart services (with mode: always they will reload)
```

---

## Quick Test

After loading data, test with:

**Login**: `james.wilson@email.ie` / `Egmqr123k*`

You should see:
- 15 Dublin shops on the shop list
- Services for each shop (€15-€95)
- Sample appointments on dashboard (past/present/future with **current dates**)

---

## Password Information

**All Users Password**: `Egmqr123k*`

This includes:
- All 15 shop owners (`owner1@barbershop.ie` through `owner15@barbershop.ie`)
- All 20 customers (`james.wilson@email.ie`, `emma.brown@email.ie`, etc.)

---

## Troubleshooting

**"Script execution failed"**
- Check MySQL is running
- Verify databases exist (user_service_db, shop_service_db, appointment_service_db)
- Check for foreign key constraint errors (load order: user → shop → appointment)

**"Duplicate entry"**
- Data already loaded. Either:
  - Keep `mode: never` (data persists, no reload needed)
  - OR manually delete data first, then reload

**Data not showing**
- Verify `mode: always` was set before startup
- Check application logs for SQL execution
- Confirm no errors in console output
