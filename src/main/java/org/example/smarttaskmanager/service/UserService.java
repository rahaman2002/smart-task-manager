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

import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    // ================= REGISTER USER =================
    public User registerUser(String username, String email, String password) {

        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("User already exists");
        }

        User user = User.builder()
                .username(username) // display name
                .email(email)       // identity
                .password(passwordEncoder.encode(password))
                .roles(Set.of(Role.ROLE_USER))
                .build();

        return userRepository.save(user);
    }

    // ================= FIND USER BY EMAIL =================
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // ================= GET USER FROM JWT =================
    public User getUserFromToken(String token) {

        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // JWT subject now stores EMAIL
        String email = jwtTokenProvider.getEmailFromToken(token);

        return getUserByEmail(email);
    }

    // ================= GET CURRENT AUTHENTICATED USER =================
    public User getCurrentUser() {

        Authentication auth = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("No authenticated user found");
        }

        // Spring stores EMAIL as principal name
        String email = auth.getName();

        return getUserByEmail(email);
    }
}