# Quick Reference: Using RestTemplate Clients

## 📞 How to Use in Your Code

### **Step 1: Inject the Client**

```java
@Service
public class YourService {
    
    private final ShopServiceClient shopClient;
    private final UserServiceClient userClient;
    
    // Constructor injection (Spring does this automatically)
    public YourService(ShopServiceClient shopClient, UserServiceClient userClient) {
        this.shopClient = shopClient;
        this.userClient = userClient;
    }
}
```

### **Step 2: Call the Methods**

```java
// Get shop details
ShopDto shop = shopClient.getShop(5L);
System.out.println(shop.name());        // "Premium Cuts"
System.out.println(shop.address());     // "123 Main St"
System.out.println(shop.openingTime()); // "09:00"

// Get service details
ServiceDto service = shopClient.getService(10L);
System.out.println(service.name());     // "Haircut"
System.out.println(service.price());    // 25.00
System.out.println(service.duration()); // 30 (minutes)

// Get employee details
EmployeeDto employee = shopClient.getEmployee(3L);
System.out.println(employee.name());    // "John Doe"
System.out.println(employee.role());    // "Senior Barber"

// Get customer details
UserDto customer = userClient.getUser(123L);
System.out.println(customer.getFullName()); // "Jane Smith"
System.out.println(customer.email());       // "jane@example.com"
```

---

## 🎯 Common Use Cases

### **1. Creating Appointment (Validation)**

```java
public void createAppointment(AppointmentRequestDto request, Long customerId) {
    // Validate shop exists
    ShopDto shop = shopClient.getShop(request.shopId());
    
    // Validate service exists and belongs to shop
    ServiceDto service = shopClient.getService(request.serviceId());
    if (!service.shopId().equals(request.shopId())) {
        throw new RuntimeException("Service doesn't belong to this shop!");
    }
    
    // If everything is valid, save appointment
    // ...
}
```

### **2. Displaying Appointment to Customer**

```java
public AppointmentResponseDto getAppointmentForCustomer(Long appointmentId) {
    // Get appointment from database
    Appointment apt = appointmentRepository.findById(appointmentId);
    
    // Fetch related data
    ShopDto shop = shopClient.getShop(apt.getShopId());
    ServiceDto service = shopClient.getService(apt.getServiceId());
    EmployeeDto employee = shopClient.getEmployee(apt.getEmployeeId());
    
    // Build response
    return new AppointmentResponseDto(
        apt.getId(),
        apt.getAppointmentDate(),
        apt.getAppointmentTime(),
        apt.getStatus(),
        shop.name(),
        shop.address(),
        service.name(),
        service.price(),
        employee != null ? employee.name() : "Any Available"
    );
}
```

### **3. Displaying Appointment to Shop Owner**

```java
public AppointmentResponseDto getAppointmentForOwner(Long appointmentId) {
    // Get appointment
    Appointment apt = appointmentRepository.findById(appointmentId);
    
    // Fetch customer details (owner needs contact info)
    UserDto customer = userClient.getUser(apt.getCustomerId());
    
    // Fetch service details
    ServiceDto service = shopClient.getService(apt.getServiceId());
    
    // Build response with customer info
    return new AppointmentResponseDto(
        apt.getId(),
        apt.getAppointmentDate(),
        apt.getAppointmentTime(),
        customer.getFullName(),
        customer.email(),
        customer.phone(),
        service.name(),
        service.price()
    );
}
```

---

## ⚠️ Error Handling

### **Handle Service Unavailability:**

```java
try {
    ShopDto shop = shopClient.getShop(shopId);
} catch (RuntimeException e) {
    // Service is down or shop doesn't exist
    throw new ServiceUnavailableException("Unable to fetch shop details");
}
```

### **Handle Null Employee:**

```java
Long employeeId = appointment.getEmployeeId();

String employeeName;
if (employeeId != null) {
    EmployeeDto employee = shopClient.getEmployee(employeeId);
    employeeName = employee.name();
} else {
    employeeName = "Any Available Employee";
}
```

---

## 🧪 Testing Tips

### **Check if Cache is Working:**

```java
// Run this twice - second time should be MUCH faster
long start = System.currentTimeMillis();
ShopDto shop = shopClient.getShop(5L);
long end = System.currentTimeMillis();
System.out.println("Time: " + (end - start) + "ms");

// First run: ~200ms
// Second run: ~2ms ✨
```

### **See Cache Logs:**

Run your app and watch console:
```
2025-11-11 10:30:00 DEBUG ShopServiceClient : Fetching shop 5 from: http://localhost:8082/api/shops/5
```

If you see this log → Cache MISS (making HTTP call)
If you DON'T see it → Cache HIT (using cached data) ✅

---

## 📝 Complete Example

```java
@Service
public class AppointmentService {
    
    private final AppointmentRepository appointmentRepository;
    private final ShopServiceClient shopClient;
    private final UserServiceClient userClient;
    
    public AppointmentService(
            AppointmentRepository appointmentRepository,
            ShopServiceClient shopClient,
            UserServiceClient userClient) {
        this.appointmentRepository = appointmentRepository;
        this.shopClient = shopClient;
        this.userClient = userClient;
    }
    
    public AppointmentResponseDto createAppointment(
            AppointmentRequestDto request, 
            Long customerId) {
        
        // 1. Validate using clients
        ShopDto shop = shopClient.getShop(request.shopId());
        ServiceDto service = shopClient.getService(request.serviceId());
        
        // 2. Create appointment entity (IDs only)
        Appointment appointment = new Appointment();
        appointment.setCustomerId(customerId);
        appointment.setShopId(request.shopId());
        appointment.setServiceId(request.serviceId());
        appointment.setAppointmentDate(request.appointmentDate());
        appointment.setAppointmentTime(request.appointmentTime());
        appointment.setStatus(AppointmentStatus.PENDING);
        
        // 3. Save to database
        appointment = appointmentRepository.save(appointment);
        
        // 4. Build enriched response
        UserDto customer = userClient.getUser(customerId);
        
        return new AppointmentResponseDto(
            appointment.getId(),
            appointment.getAppointmentDate(),
            appointment.getAppointmentTime(),
            appointment.getStatus(),
            appointment.getNotes(),
            customer.id(),
            customer.getFullName(),
            customer.email(),
            customer.phone(),
            shop.id(),
            shop.name(),
            shop.address(),
            shop.phone(),
            service.id(),
            service.name(),
            service.price(),
            service.duration(),
            null,  // employeeId
            null,  // employeeName
            appointment.getCreatedAt()
        );
    }
}
```

---

## 🎓 Remember

1. **Always inject clients via constructor**
2. **Use clients to validate data before saving**
3. **Use clients to enrich responses with current data**
4. **Don't worry about caching - Spring handles it**
5. **Watch logs to see cache working**

---

That's it! You now have a complete understanding of how to use RestTemplate clients in your microservice. 🎉
