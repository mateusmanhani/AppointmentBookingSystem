package com.barbershop.shop_service.repository;

import com.barbershop.shop_service.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    
    /**
     * Find all employees for a specific shop.
     */
    List<Employee> findByShopId(Long shopId);
    
    /**
     * Find a specific employee by id and shopId.
     */
    Optional<Employee> findByIdAndShopId(Long id, Long shopId);
}
