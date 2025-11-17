# ✅ Mock Data Configuration Complete!

## Summary of Changes

### 🔑 Password Update
**All 35 users now use**: `Egmqr123k*`

- ✅ 15 Shop Owners (owner1@barbershop.ie through owner15@barbershop.ie)
- ✅ 20 Customers (james.wilson@email.ie, emma.brown@email.ie, etc.)

**BCrypt Hash**: `$2a$12$aOvmhzdxSx1/9ahB9CndpuWS7PLOYYnf6ZPiPlc14GsutlgucUCWm`

---

### 🔄 Dynamic Loading Enabled

All 3 services configured with `spring.sql.init.mode: always`

**What this means**:
- ✅ Data **automatically resets** on every service restart
- ✅ Dates are **dynamically calculated** from `CURDATE()` (always current)
- ✅ Perfect for **testing and demos** (fresh data every time)
- ✅ No stale appointments or outdated dates

**Services configured**:
1. ✅ `user-service/application-local.yml`
2. ✅ `shop-service/application.yml`
3. ✅ `appointment-service/application.yaml`

---

### 📊 Data Overview

**15 Dublin Shops** with real GPS coordinates:
- Temple Bar, O'Connell Street, Rathmines, Ballsbridge, etc.
- Spanning North, South, City Center, and Coastal Dublin

**75 Services** (5 per shop):
- Prices: €15 - €95
- Duration: 15 - 90 minutes
- Mix of cuts, fades, shaves, and packages

**50 Employees** (3-4 per shop):
- Irish names and contact details
- Master, Senior, Head, and Junior Barbers

**~75 Appointments**:
- Past (completed/cancelled) - 2 weeks ago to yesterday
- Today - 4 confirmed appointments
- Future (confirmed/pending) - tomorrow to 4 weeks ahead
- All dates **relative to current date**

---

## 🚀 How to Use

### Starting Services
```bash
# Just start the services normally
mvn spring-boot:run
```

Data loads automatically with:
- Fresh dates relative to today
- All users ready with password `Egmqr123k*`
- 15 Dublin shops with real coordinates ready for map

### Test Login
**Customer**: `james.wilson@email.ie` / `Egmqr123k*`  
**Shop Owner**: `owner1@barbershop.ie` / `Egmqr123k*`

### Every Restart
- Data is **deleted and recreated**
- Dates recalculated from current date
- Perfect for clean testing sessions

---

## 📝 Notes

### When to Restart Services
Restart whenever you want:
- ✅ Fresh appointment dates
- ✅ Clean slate for testing
- ✅ Remove test bookings made during development

### Making Data Persistent (Optional)
If you need data to persist across restarts:
1. Change `mode: always` → `mode: never` in all 3 config files
2. Manual bookings will be kept
3. Dates will remain static

---

## 🗺️ Ready for Map Implementation

All shops have GPS coordinates ready:
```
Shop 1: The Grafton Barber → 53.342778, -6.264167
Shop 2: Northside Cuts → 53.351389, -6.260556
...
Shop 15: Clontarf Clippers → 53.363889, -6.216667
```

**Map center point**: Dublin City (53.349805, -6.260310)

---

## 📚 Documentation

- **Full Data Details**: `docs/MOCK_DATA_README.md`
- **Setup Guide**: `docs/SETUP_MOCK_DATA.md`
- **SQL Files**: 
  - `user-service/src/main/resources/data.sql`
  - `shop-service/src/main/resources/data.sql`
  - `appointment-service/src/main/resources/data.sql`

---

**All set for Sprint 4: Real-Time Availability Map!** 🎉
