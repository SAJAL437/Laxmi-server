package com.app.Repository;

import com.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository interface for User entity
 */
public interface UserRepo extends JpaRepository<User, String> {

    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phone);

    Optional<User> findById(String id);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

}