package com.barbershop.shop_service.service;

import com.barbershop.shop_service.dto.EmployeeRequestDto;
import com.barbershop.shop_service.dto.EmployeeUpdateDto;
import com.barbershop.shop_service.entity.Employee;
import com.barbershop.shop_service.repository.EmployeeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class EmployeeService {
    
    private final EmployeeRepository employeeRepository;
    
    /**
     * Add a new employee to a shop.
     */
    public Employee addEmployee(Long shopId, EmployeeRequestDto dto) {
        log.info("Adding new employee '{}' to shop id: {}", dto.name(), shopId);
        
        Employee employee = new Employee();
        employee.setShopId(shopId);
        employee.setName(dto.name());
        employee.setRole(dto.role());
        employee.setEmail(dto.email());
        employee.setPhone(dto.phone());
        
        return employeeRepository.save(employee);
    }
    
    /**
     * Get all employees for a shop.
     */
    public List<Employee> getEmployeesByShopId(Long shopId) {
        log.info("Fetching all employees for shop id: {}", shopId);
        return employeeRepository.findByShopId(shopId);
    }
    
    /**
     * Get a specific employee by id and shopId.
     */
    public Optional<Employee> getEmployeeByIdAndShopId(Long employeeId, Long shopId) {
        log.info("Fetching employee id: {} for shop id: {}", employeeId, shopId);
        return employeeRepository.findByIdAndShopId(employeeId, shopId);
    }
    
    /**
     * Update an employee's information.
     * Supports partial updates - only provided fields will be updated.
     */
    public Employee updateEmployee(Long employeeId, Long shopId, EmployeeUpdateDto dto) {
        log.info("Updating employee id: {} for shop id: {}", employeeId, shopId);
        
        Employee employee = employeeRepository.findByIdAndShopId(employeeId, shopId)
            .orElseThrow(() -> new RuntimeException("Employee not found"));
        
        // Update only non-null fields
        if (dto.name() != null) {
            employee.setName(dto.name());
        }
        if (dto.role() != null) {
            employee.setRole(dto.role());
        }
        if (dto.email() != null) {
            employee.setEmail(dto.email());
        }
        if (dto.phone() != null) {
            employee.setPhone(dto.phone());
        }
        
        return employeeRepository.save(employee);
    }
    
    /**
     * Delete an employee from a shop.
     */
    public boolean deleteEmployee(Long employeeId, Long shopId) {
        log.info("Deleting employee id: {} from shop id: {}", employeeId, shopId);
        
        Optional<Employee> employee = employeeRepository.findByIdAndShopId(employeeId, shopId);
        if (employee.isEmpty()) {
            return false;
        }
        
        employeeRepository.delete(employee.get());
        return true;
    }
}
