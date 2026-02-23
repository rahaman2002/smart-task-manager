package org.example.smarttaskmanager.controller;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final AuthenticationManager authManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    // ================= LOGIN =================
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {

            User user = userRepository.findByEmail(request.email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            // If Google user and password not set
            if (user.getPassword() == null || user.getPassword().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Please login using Google or set a password first.");
            }

            authenticateUser(request.email, request.password);

            LocalDateTime previousLastLogin = user.getLastLogin();

            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            String token = generateTokenForUser(user);

            return ResponseEntity.ok(
                    new LoginResponse(
                            token,
                            user.getUsername(),
                            previousLastLogin
                    )
            );

        } catch (UsernameNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }

    // ================= SET PASSWORD =================
    // ================= SET PASSWORD =================
    @PostMapping("/set-password")
    public ResponseEntity<?> setPassword(@RequestBody SetPasswordRequest request) {

        try {

            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            // If password already exists → block
            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body("Password already set");
            }

            // Save previous last login
            LocalDateTime previousLastLogin = user.getLastLogin();

            // Encode and save password
            user.setPassword(passwordEncoder.encode(request.getPassword()));

            // Update last login (because user is completing auth flow)
            user.setLastLogin(LocalDateTime.now());

            userRepository.save(user);

            // Authenticate immediately
            authenticateUser(request.getEmail(), request.getPassword());

            // Generate JWT
            String token = generateTokenForUser(user);

            return ResponseEntity.ok(
                    new LoginResponse(
                            token,
                            user.getUsername(),
                            previousLastLogin
                    )
            );

        } catch (UsernameNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Something went wrong");
        }
    }
    
    // ================= REGISTER =================
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            // 1️⃣ Create user with email + username + password
            User user = userService.registerUser(
                    request.username,
                    request.email,
                    request.password
            );

            // 2️⃣ Authenticate immediately using EMAIL
            authenticateUser(request.email, request.password);

            // 3️⃣ Update last login
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            // 4️⃣ Generate JWT using EMAIL
            String token = generateTokenForUser(user);

            return ResponseEntity.ok(
                    new LoginResponse(
                            token,
                            user.getUsername(),
                            null
                    )
            );

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("User already exists");
        }
    }

    // ================= COMMON METHODS =================

    private void authenticateUser(String email, String password) {
        Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private String generateTokenForUser(User user) {
        // EMAIL goes inside JWT
        return jwtTokenProvider.generateToken(
                user.getEmail(),
                user.getRoles()
        );
    }

    // ================= REQUEST / RESPONSE CLASSES =================

    public static class RegisterRequest {
        public String username;  // display name
        public String email;     // identity
        public String password;
    }

    public static class LoginRequest {
        public String email;     // login with email
        public String password;
    }

    public static class LoginResponse {
        public String token;
        public String username;
        public LocalDateTime previousLastLogin;

        public LoginResponse(String token, String username, LocalDateTime previousLastLogin) {
            this.token = token;
            this.username = username;
            this.previousLastLogin = previousLastLogin;
        }
    }

    @Setter
    @Getter
    public static class SetPasswordRequest {
        private String email;
        private String password;

    }
}