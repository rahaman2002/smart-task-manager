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

        // Use Google email as username
        String email = oAuth2User.getAttribute("email");

        // Check if user exists
        User user = userRepository.findByUsername(email)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setUsername(email);   // ✅ store email as username
                    newUser.setPassword("");      // no password for Google users
                    newUser.setRoles(Set.of(Role.ROLE_USER));
                    return userRepository.save(newUser);
                });

        // Generate JWT
        String token = jwtTokenProvider.generateToken(
                user.getUsername(),
                user.getRoles()
        );

        response.sendRedirect(
                "http://localhost:4200/login-success?token=" + token
        );
    }
}