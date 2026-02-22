package org.example.smarttaskmanager.controller;

import lombok.RequiredArgsConstructor;
import org.example.smarttaskmanager.model.User;
import org.example.smarttaskmanager.repository.UserRepository;
import org.example.smarttaskmanager.security.JwtTokenProvider;
import org.example.smarttaskmanager.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    // ================= LOGIN =================
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            // Authenticate
            authenticateUser(request.username, request.password);

            // Fetch user
            User user = userRepository.findByUsername(request.username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            // Save previous last login
            LocalDateTime previousLastLogin = user.getLastLogin();

            // Update last login
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            // Generate token
            String token = generateTokenForUser(user);

            // Return token + previous last login
            return ResponseEntity.ok(new LoginResponse(token, previousLastLogin));

        } catch (UsernameNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }

    // ================= REGISTER =================
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            // 1️⃣ Create user
            User user = userService.registerUser(request.username, request.password);

            // 2️⃣ Authenticate immediately
            authenticateUser(request.username, request.password);

            // 3️⃣ Update last login
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            // 4️⃣ Generate token
            String token = generateTokenForUser(user);

            // Return token (no previous login since it's first login)
            return ResponseEntity.ok(new LoginResponse(token, null));

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("User already exists");
        }
    }

    // ================= COMMON METHODS =================
    private void authenticateUser(String username, String password) {
        Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private String generateTokenForUser(User user) {
        return jwtTokenProvider.generateToken(user.getUsername(), user.getRoles());
    }

    // ================= REQUEST / RESPONSE CLASSES =================
    public static class RegisterRequest {
        public String username;
        public String password;
    }

    public static class LoginRequest {
        public String username;
        public String password;
    }

    public static class LoginResponse {
        public String token;
        public LocalDateTime previousLastLogin;

        public LoginResponse(String token, LocalDateTime previousLastLogin) {
            this.token = token;
            this.previousLastLogin = previousLastLogin;
        }
    }
}