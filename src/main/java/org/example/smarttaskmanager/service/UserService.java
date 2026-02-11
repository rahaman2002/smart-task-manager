package com.example.smarttaskmanager.service;

import com.example.smarttaskmanager.model.User;
import com.example.smarttaskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // ===== Register user =====
    public User registerUser(String username, String password) {
        // Check if user exists
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("User already exists");
        }
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password)) // always encode
                .roles(new HashSet<>()) // default empty roles
                .build();
        return userRepository.save(user);
    }

    // ===== Load user by username =====
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
