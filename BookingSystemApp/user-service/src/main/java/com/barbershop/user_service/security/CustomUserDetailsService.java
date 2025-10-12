package com.barbershop.user_service.security;

import com.barbershop.user_service.entity.User;
import com.barbershop.user_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collections;

public class CustomUserDetailsService  implements UserDetailsService {

    @Autowired
    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * This method is called by Spring Security to load user information
     * It converts our Database User entity into Spring Security's UserDetails format
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Find the user in the database with the username passes (user email in this case)
        User user = userRepository.findActiveUserByEmail(username.toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail()) // set username
                .password(user.getPassword()) // set password
                .authorities(Collections.singletonList( // Set permissions/roles
                        new SimpleGrantedAuthority("ROLE_" +user.getRole().name())
                ))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!user.getActive())
                .build(); // create UserDetails object
    }
}
