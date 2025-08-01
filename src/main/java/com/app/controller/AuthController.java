package com.app.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.app.DTOs.AuthResponse;
import com.app.DTOs.LoginRequest;
import com.app.DTOs.SignupRequest;
import com.app.Repository.RoleRepo;
import com.app.Repository.UserRepo;
import com.app.config.JwtUtils;
import com.app.entity.User;
import com.app.exception.UserException;
import com.app.service.CartService;
import com.app.service.UserServices;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepo userRepo;
    private final RoleRepo roleRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;
    private final UserServices userService;

    public AuthController(UserRepo userRepo, RoleRepo roleRepo, PasswordEncoder passwordEncoder, JwtUtils jwtUtils,
            UserDetailsService userDetailsService, UserServices userService) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
        this.userService = userService;
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> createUserHandler(@Valid @RequestBody SignupRequest signupRequest)
            throws UserException {

        userService.CreateUser(signupRequest);

        Authentication authentication = authenticate(signupRequest.getContact(), signupRequest.getPassword());

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtUtils.generateToken(authentication);

        return new ResponseEntity<>(AuthResponse.builder()
                .token(token)
                .status(true)
                .message("Registration successful! Please verify your OTP.")
                .build(), HttpStatus.CREATED);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<AuthResponse> verifyOtp(@RequestParam String contact, @RequestParam String otp) {
        boolean verified = userService.verifyOtp(contact, otp);
        if (verified) {
            User user = userService.getUserByEmail(contact);
            if (user == null) {
                user = userService.getUserByPhone(contact);
            }
            if (user == null) {
                return ResponseEntity.badRequest().body(AuthResponse.builder()
                        .message("No user found with this contact")
                        .status(false)
                        .build());
            }

            // Instead of using password check (since it's already encoded),
            // directly create authentication from userDetails
            UserDetails userDetails = userDetailsService.loadUserByUsername(
                    user.getEmail() != null ? user.getEmail() : user.getPhone());

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authentication);

            String token = jwtUtils.generateToken(authentication);

            return ResponseEntity.ok(AuthResponse.builder()
                    .token(token)
                    .status(true)
                    .message("✅ OTP verified successfully")
                    .build());
        } else {
            return ResponseEntity.badRequest().body(AuthResponse.builder()
                    .message("Invalid OTP or contact")
                    .status(false)
                    .build());
        }
    }

    @PostMapping("/signin")
    public ResponseEntity<AuthResponse> signin(@RequestBody LoginRequest loginRequest) {
        try {
            String contact = loginRequest.getContact();
            if (contact == null || contact.trim().isEmpty()) {
                throw new UserException("Contact is required.");
            }

            boolean isEmail = contact.matches("^\\S+@\\S+\\.\\S+$");
            boolean isPhone = contact.matches("^\\d{10}$");

            User user;

            if (isEmail) {
                user = userService.getUserByEmail(contact);
                if (user == null)
                    throw new UserException("No user found with this email.");
                if (!Boolean.TRUE.equals(user.getIsEmailVerified())) {
                    throw new UserException("Email not verified.");
                }
            } else if (isPhone) {
                user = userService.getUserByPhone(contact);
                if (user == null)
                    throw new UserException("No user found with this phone number.");
                if (!Boolean.TRUE.equals(user.getIsPhoneVerified())) {
                    throw new UserException("Phone not verified.");
                }
            } else {
                throw new UserException("Invalid contact format (must be email or 10-digit phone).");
            }

            // Authenticate with user’s email if it exists; otherwise fallback to phone
            String loginUsername = user.getEmail() != null ? user.getEmail() : user.getPhone();

            Authentication authentication = authenticate(loginUsername, loginRequest.getPassword());

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = jwtUtils.generateToken(authentication);

            return ResponseEntity.ok(AuthResponse.builder()
                    .token(token)
                    .status(true)
                    .message("Sign-in successful")
                    .build());

        } catch (UserException | BadCredentialsException e) {
            return new ResponseEntity<>(AuthResponse.builder()
                    .message(e.getMessage())
                    .status(false)
                    .build(), HttpStatus.UNAUTHORIZED);
        }
    }

    private Authentication authenticate(String username, String password) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        if (userDetails == null || !passwordEncoder.matches(password, userDetails.getPassword())) {
            throw new BadCredentialsException("Invalid username or password");
        }
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }
}