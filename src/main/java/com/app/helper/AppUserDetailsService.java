package com.app.helper;

import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.app.Repository.UserRepo;
import com.app.entity.User;

@Service
public class AppUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepo userRepository;

    @Override
    public UserDetails loadUserByUsername(String contact) throws UsernameNotFoundException {
        // Try to find by email first
        User user = userRepository.findByEmail(contact)
                .or(() -> userRepository.findByPhone(contact)) // If not email, try phone
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with email or phone: " + contact));

        return new org.springframework.security.core.userdetails.User(
                contact, // still use the original input as username (email or phone)
                user.getPassword(),
                user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                        .collect(Collectors.toList()));
    }

    
}
