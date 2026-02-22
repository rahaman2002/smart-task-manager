package org.example.smarttaskmanager.service;

import lombok.RequiredArgsConstructor;
import org.example.smarttaskmanager.model.Role;
import org.example.smarttaskmanager.model.User;
import org.example.smarttaskmanager.repository.UserRepository;
import org.example.smarttaskmanager.security.JwtTokenProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    // Register user
    public User registerUser(String username, String password) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("User already exists");
        }
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .roles(new HashSet<>())
                .build();
        user.setRoles(Set.of(Role.ROLE_USER));
        return userRepository.save(user);
    }

    // Get user by username
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    //  Get user from JWT token
    public User getUserFromToken(String token) {
        if (token.startsWith("Bearer ")) token = token.substring(7);
        String username = jwtTokenProvider.getUsernameFromToken(token);
        return getUserByUsername(username);
    }

    //  Get currently authenticated user
    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("No authenticated user found");
        }

        String username = auth.getName(); // Spring Security stores username here
        return getUserByUsername(username);
    }
}
