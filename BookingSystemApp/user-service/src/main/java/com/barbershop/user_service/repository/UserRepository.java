package com.barbershop.user_service.repository;

import com.barbershop.user_service.entity.User;
import com.barbershop.user_service.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 /**
 * Repository interface for User entity
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * Find user by email for login and registration validation
     * @param email
     * @return User object if there is any or a NoSuchElementException
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if email already exists
     */
    boolean existsByEmail(String email);

    /**
     * Find active users by role
     */
    List<User> findByRoleAndIsActiveTrue(UserRole role);

    /**
     * Find active user by email
     */
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.isActive = true")
    Optional<User> findActiveUserByEmail(@Param("email") String email);

    /**
     * Count users by role
     */
    long countByRole(UserRole role);

    /**
     * Search users by name (case-insentive)
     */
    @Query("SELECT u FROM User u WHERE " +
            "LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "AND u.isActive = true")
    List<User> findByNameContainingIgnoreCase(@Param("searchTerm") String searchTerm);
    


}
