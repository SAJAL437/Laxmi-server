package com.app.controller;

import com.app.entity.User;
import com.app.exception.UserException;
import com.app.service.UserServices;

import jakarta.validation.constraints.Min;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/admin/user")
public class AdminUserController {
    private static final Logger log = LoggerFactory.getLogger(AdminUserController.class);
    private static final String[] VALID_SORT_FIELDS = { "id", "name", "email", "createdAt" };
    private final UserServices userServices;

    public AdminUserController(UserServices userServices) {
        this.userServices = userServices;
    }

    @GetMapping("/")
    public ResponseEntity<List<User>> getAllUsers(@RequestHeader("Authorization") String Jwt) throws UserException {
        List<User> user = userServices.findAllUser();
        return new ResponseEntity<>(user, HttpStatus.ACCEPTED);
    }

    @GetMapping("/role-user")
    public ResponseEntity<Page<User>> getAllUsersWithUserRole(
            @RequestHeader("Authorization") String jwt,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            @RequestParam(defaultValue = "id") String sortBy) throws UserException {

        log.info("Received request for get-all-users-with-user-role: page={}, size={}, sortBy={}, jwt={}", page, size,
                sortBy, jwt != null ? "provided" : "missing");

        // Validate sortBy field (case-insensitive)
        String sortField = sortBy.toLowerCase();
        if (!Arrays.asList(VALID_SORT_FIELDS).contains(sortField)) {
            log.warn("Invalid sort field provided: {}", sortBy);
            throw new UserException(
                    "Invalid sort field: " + sortBy + ". Valid fields are: " + Arrays.toString(VALID_SORT_FIELDS));
        }

        // Create Pageable object
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortField));
        Page<User> users = userServices.getAllUsersWithUserRole(pageable);

        log.info("Successfully fetched {} users with USER role for page {} with size {}", users.getTotalElements(),
                page, size);
        return ResponseEntity.ok(users);
    }
}