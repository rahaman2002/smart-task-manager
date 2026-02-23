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
@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public OAuth2SuccessHandler(UserRepository userRepository, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setUsername(name != null ? name : email.split("@")[0]);
                    newUser.setPassword("");
                    newUser.setRoles(Set.of(Role.ROLE_USER));
                    newUser.setLastLogin(LocalDateTime.now());
                    return userRepository.save(newUser);
                });

        // Capture previous login BEFORE updating
        LocalDateTime previousLastLogin = user.getLastLogin();

        // Update last login to now
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        // If password not set → go to set-password page
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            response.sendRedirect(
                    "http://localhost:4200/set-password?email=" + email
            );
            return;
        }

        String token = jwtTokenProvider.generateToken(user.getEmail(), user.getRoles());

        String redirectUrl = String.format(
                "http://localhost:4200/login-success?token=%s&username=%s&previousLastLogin=%s",
                token,
                user.getUsername(),
                previousLastLogin != null ? previousLastLogin : ""
        );

        response.sendRedirect(redirectUrl);
    }
}