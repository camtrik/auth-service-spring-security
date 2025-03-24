package com.ebbilogue.authservice.config;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.ebbilogue.authservice.models.ERole;
import com.ebbilogue.authservice.models.Role;
import com.ebbilogue.authservice.models.User;
import com.ebbilogue.authservice.repository.RoleRepository;
import com.ebbilogue.authservice.repository.UserRepository;

/**
 * 初始化管理员
 */
@Component
public class InitializationConfig {
    @Autowired
    private UserRepository userRepository; 

    @Autowired
    private RoleRepository roleRepository; 

    @Autowired
    private PasswordEncoder passwordEncoder; 

    @Value("${app.admin.username}")
    private String adminUsername; 

    @Value("${app.admin.email}")
    private String adminEmail; 

    @Value("${app.admin.password}")
    private String adminPassword; 
    
    @EventListener(ApplicationReadyEvent.class)
    public void initializeAdmin() { 
        for (ERole role : ERole.values()) {
            if (!roleRepository.existsByName(role)) {
                roleRepository.save(new Role(role));
            }
        }

        if (!userRepository.existsByUsername(adminUsername)) { 
            User adminUser = new User( 
                adminUsername, 
                adminEmail, 
                passwordEncoder.encode(adminPassword)
            ); 

            Set<Role> roles = new HashSet<>(); 
            Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                .orElseThrow(() -> new RuntimeException("Error: Admin role is not found.")); 
            roles.add(adminRole);
            adminUser.setRoles(roles); 

            userRepository.save(adminUser); 
        }
    }
}
