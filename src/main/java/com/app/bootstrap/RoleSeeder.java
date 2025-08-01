package com.app.bootstrap;

import com.app.DTOs.ERole;
import com.app.entity.Role;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RoleSeeder {

    private final com.app.Repository.RoleRepo roleRepo;

    @PostConstruct
    public void seedRoles() {
        Arrays.stream(ERole.values()).forEach(roleEnum -> {
            roleRepo.findByName(roleEnum).orElseGet(() -> {
                Role role = Role.builder()
                        .id(UUID.randomUUID().toString())
                        .name(roleEnum)
                        .build();
                roleRepo.save(role);
                System.out.println("✅ Role " + roleEnum + " created.");
                return role;
            });
        });
    }
}
