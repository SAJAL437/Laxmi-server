package com.app.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.DTOs.ERole;
import com.app.entity.Role;

@Repository
public interface RoleRepo extends JpaRepository<Role, String> {
    Optional<Role> findByName(ERole name);
}

