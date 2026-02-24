package org.example.smarttaskmanager.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.smarttaskmanager.model.Role;
import org.example.smarttaskmanager.model.User;
import org.example.smarttaskmanager.repository.UserRepository;
import org.example.smarttaskmanager.security.JwtTokenProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
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
                                        Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        // Find existing user or create new one
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setUsername(name != null ? name : email.split("@")[0]);
            newUser.setPassword(""); // empty → must set password
            newUser.setRoles(Set.of(Role.ROLE_USER)); // keep Role type
            newUser.setLastLogin(LocalDateTime.now());
            return userRepository.save(newUser);
        });

        // Capture previous login before updating
        LocalDateTime previousLastLogin = user.getLastLogin();

        // Update last login
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        // Redirect to set-password page if password is empty
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            response.sendRedirect(
                    "https://smart-task-manager-chi.vercel.app/set-password?email=" + email
            );
            return;
        }

        // Generate JWT token
        String token = jwtTokenProvider.generateToken(user.getEmail(), user.getRoles());

        // Redirect to Angular frontend with token
        String redirectUrl = String.format(
                "https://smart-task-manager-chi.vercel.app/login-success?token=%s&username=%s&previousLastLogin=%s",
                token,
                user.getUsername(),
                previousLastLogin != null ? previousLastLogin : ""
        );

        response.sendRedirect(redirectUrl);
    }
}