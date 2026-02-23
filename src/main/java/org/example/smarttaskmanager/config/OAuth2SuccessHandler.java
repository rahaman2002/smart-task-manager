package org.example.smarttaskmanager.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.example.smarttaskmanager.model.Role;
import org.example.smarttaskmanager.model.User;
import org.example.smarttaskmanager.repository.UserRepository;
import org.example.smarttaskmanager.security.JwtTokenProvider;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Set;

@Component // Registers this as a Spring bean automatically
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    // Used to generate JWT tokens
    private final JwtTokenProvider jwtTokenProvider;

    // Used to check/create users in database
    private final UserRepository userRepository;

    // Constructor injection (Spring injects dependencies)
    public OAuth2SuccessHandler(JwtTokenProvider jwtTokenProvider,
                                UserRepository userRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
    }

    /**
     * This method runs automatically AFTER Google login succeeds.
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        // Check if user exists
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    // Register new user
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setUsername(name != null ? name : email.split("@")[0]);
                    newUser.setPassword(""); // Google user, no password
                    newUser.setRoles(Set.of(Role.ROLE_USER));
                    newUser.setLastLogin(LocalDateTime.now());
                    return userRepository.save(newUser);
                });

        // Save previous last login
        LocalDateTime previousLastLogin = user.getLastLogin();

        // Update lastLogin for current login
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        // Generate JWT
        String token = jwtTokenProvider.generateToken(user.getEmail(), user.getRoles());

        // Redirect to Angular with token, username, previous login
        String redirectUrl = String.format(
                "http://localhost:4200/login-success?token=%s&username=%s&previousLastLogin=%s",
                token,
                user.getUsername(),
                previousLastLogin != null ? previousLastLogin.toString() : ""
        );

        response.sendRedirect(redirectUrl);
    }
}