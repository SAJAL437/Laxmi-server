package com.app.service.impl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.app.DTOs.ERole;
import com.app.DTOs.SignupRequest;
import com.app.Repository.RoleRepo;
import com.app.Repository.UserRepo;
import com.app.config.JwtUtils;
import com.app.entity.Role;
import com.app.entity.User;
import com.app.exception.UserException;
import com.app.helper.OtpGenerator;
import com.app.service.CartService;
import com.app.service.UserServices;

@Service
public class UserServiceImpl implements UserServices {

    private final UserRepo userRepo;
    private final RoleRepo roleRepo;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final CartService cartService;

    public UserServiceImpl(UserRepo userRepo, CartService cartService, JwtUtils jwtUtils,
            PasswordEncoder passwordEncoder, RoleRepo roleRepo) {
        this.userRepo = userRepo;
        this.jwtUtils = jwtUtils;
        this.roleRepo = roleRepo;
        this.passwordEncoder = passwordEncoder;
        this.cartService = cartService;
    }

    @Autowired
    private JavaMailSender javaMailSender;

    // In-memory OTP store: key = contact, value = OTP
    private final Map<String, String> otpStore = new HashMap<>();

    @Override
    public void CreateUser(SignupRequest request) throws UserException {
        String contact = request.getContact();
        boolean isEmail = contact.matches("^\\S+@\\S+\\.\\S+$");
        boolean isPhone = contact.matches("^\\d{10}$");

        if (!isEmail && !isPhone) {
            throw new UserException("Invalid contact format. Use email or 10-digit phone.");
        }

        if (isEmail && userRepo.existsByEmail(contact)) {
            throw new UserException("Email already registered.");
        }

        if (isPhone && userRepo.existsByPhone(contact)) {
            throw new UserException("Phone already registered.");
        }

        Role userRole = roleRepo.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> new UserException("Default role ROLE_USER not found"));

        // Build user
        User.UserBuilder userBuilder = User.builder()
                .id(UUID.randomUUID().toString())
                .name(request.getName())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(Set.of(userRole))
                .isEmailVerified(false)
                .isPhoneVerified(false)
                .isSeller(false)
                .createdAt(LocalDateTime.now());

        if (isEmail)
            userBuilder.email(contact);
        else
            userBuilder.phone(contact);

        // User user = userBuilder.build();
        // userRepo.save(user);

        User user = userBuilder.build();

        // Save user and create cart
        userRepo.save(user);
        cartService.createCart(user);

        // Generate OTP
        String otp = OtpGenerator.generateOtp();
        otpStore.put(contact, otp);

        // Send OTP
        if (isEmail) {
            sendOtpEmail(contact, otp);
        }

        System.out.println("✅ OTP sent to " + contact + ": " + otp);
    }

    private void sendOtpEmail(String to, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Your OTP for Registration");
        message.setText("Your OTP is: " + otp + "\nThis OTP is valid for 5 minutes.");
        javaMailSender.send(message);
    }

    @Override
    public boolean verifyOtp(String contact, String submittedOtp) {
        String storedOtp = otpStore.get(contact);
        if (storedOtp != null && storedOtp.equals(submittedOtp)) {
            User user = contact.contains("@") ? userRepo.findByEmail(contact).orElse(null)
                    : userRepo.findByPhone(contact).orElse(null);

            if (user != null) {
                if (contact.contains("@"))
                    user.setIsEmailVerified(true);
                else
                    user.setIsPhoneVerified(true);

                userRepo.save(user);
                otpStore.remove(contact); // remove OTP after successful verification
                return true;
            }
        }
        return false;
    }

    @Override
    public User FindUserProfileByJwt(String jwt) throws UserException {
        if (jwt.startsWith("Bearer "))
            jwt = jwt.substring(7);
        String email = jwtUtils.extractUsername(jwt);
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new UserException("User not found with email: " + email));
    }

    @Override
    public User save(User user) throws UserException {
        return userRepo.save(user);
    }

    @Override
    public User getUserByid(String id) throws UserException {
        return userRepo.findById(id)
                .orElseThrow(() -> new UserException("User not found with this id " + id));
    }

    @Override
    public User getUserByEmail(String email) throws UserException {
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new UserException("User not found with this email " + email));
    }

    @Override
    public User getUserByPhone(String phone) throws UserException {
        return userRepo.findByPhone(phone)
                .orElseThrow(() -> new UserException("User not found with this phone " + phone));
    }

    @Override
    public User update(User user, String id) throws UserException {
        User existingUser = getUserByid(id);
        existingUser.setName(user.getName());
        existingUser.setAddress(user.getAddress());
        existingUser.setPhone(user.getPhone());
        return userRepo.save(existingUser);
    }

    @Override
    public void delete(String id) throws UserException {
        User user = getUserByid(id);
        userRepo.delete(user);
    }

    @Override
    public void generateAndSendOtp(String contact) throws UserException {

        boolean isEmail = contact.contains("@");
        User user;

        if (isEmail) {
            user = userRepo.findByEmail(contact).orElseThrow(() -> new UserException("No user found with this email"));
        } else {
            user = userRepo.findByPhone(contact)
                    .orElseThrow(() -> new UserException("No user found with this phone: " + contact));
        }

        String otp = OtpGenerator.generateOtp();
        otpStore.put(contact, otp);
        if (isEmail) {
            sendOtpEmail(contact, otp);
        } else {
            // TODO: Replace this log with actual SMS API logic (e.g., MSG91)
            System.out.println("📱 OTP for phone " + contact + ": " + otp);
        }
        System.out.println("✅ OTP sent to " + contact + ": " + otp);

    }

    @Override
    public List<User> findAllUser() throws UserException {
        return userRepo.findAll();
    }

    @Override
    public Page<User> getAllUsersWithUserRole(Pageable pageable) throws UserException {
        Role userRole = roleRepo.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Role ROLE_USER not found"));
        return userRepo.findByRoles(userRole, pageable);
    }
}
