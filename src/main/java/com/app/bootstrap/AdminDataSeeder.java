package com.app.bootstrap;

import com.app.DTOs.ERole;
import com.app.entity.Role;
import com.app.entity.User;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Component
public class AdminDataSeeder {

    @Autowired
    private com.app.Repository.UserRepo userRepo;

    @Autowired
    private com.app.Repository.RoleRepo roleRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostConstruct
    public void createAdminUserIfNotExists() {
        // Ensure ROLE_ADMIN exists
        Role adminRole = roleRepo.findByName(ERole.ROLE_ADMIN)
                .orElseGet(() -> roleRepo.save(Role.builder()
                        .id(UUID.randomUUID().toString())
                        .name(ERole.ROLE_ADMIN)
                        .build()));

        // Check if admin user with this email exists
        Optional<User> existingAdmin = userRepo.findByEmail("admin@site.com");

        if (existingAdmin.isEmpty()) {
            User admin = User.builder()
                    .id(UUID.randomUUID().toString())
                    .name("Admin")
                    .email("admin@site.com")
                    .password(passwordEncoder.encode("admin123"))
                    .roles(Set.of(adminRole))
                    .isEmailVerified(true)
                    .isPhoneVerified(true)
                    .isSeller(false)
                    .createdAt(LocalDateTime.now())
                    .build();

            userRepo.save(admin);
            System.out.println("✅ Default admin user created.");
        } else {
            System.out.println("ℹ️ Admin user already exists. Skipping creation.");
        }
    }
}
