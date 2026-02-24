package org.example.smarttaskmanager.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.example.smarttaskmanager.model.Role;
import org.example.smarttaskmanager.model.User;
import org.example.smarttaskmanager.repository.UserRepository;
import org.example.smarttaskmanager.security.JwtTokenProvider;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

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

        try {
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

            String email = oAuth2User.getAttribute("email");
            String name = oAuth2User.getAttribute("name");

            // Find user or create new
            User user = userRepository.findByEmail(email)
                    .orElseGet(() -> {
                        User newUser = new User();
                        newUser.setEmail(email);
                        newUser.setUsername(name != null ? name : email.split("@")[0]);
                        newUser.setPassword(""); // empty → force set-password
                        // Mutable HashSet for Hibernate
                        newUser.setRoles(new HashSet<>(Set.of(Role.ROLE_USER)));
                        newUser.setLastLogin(LocalDateTime.now());
                        return userRepository.save(newUser);
                    });

            // Capture previous login
            LocalDateTime previousLastLogin = user.getLastLogin();

            // Update last login to now
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            // If password is not set → redirect to set-password page
            if (user.getPassword() == null || user.getPassword().isEmpty()) {
                response.sendRedirect(
                        "https://smart-task-manager-chi.vercel.app/set-password?email=" + email
                );
                return;
            }

            // Convert Role enum to Set<String> for JWT
            Set<String> roleStrings = user.getRoles().stream()
                    .map(Role::name)
                    .collect(Collectors.toSet());

            String token = jwtTokenProvider.generateToken(user.getEmail(), roleStrings);

            // Redirect to Angular app with token
            String redirectUrl = String.format(
                    "https://smart-task-manager-chi.vercel.app/login-success?token=%s&username=%s&previousLastLogin=%s",
                    token,
                    user.getUsername(),
                    previousLastLogin != null ? previousLastLogin : ""
            );

            response.sendRedirect(redirectUrl);

        } catch (Exception ex) {
            ex.printStackTrace(); // Log error to Render logs
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }
}