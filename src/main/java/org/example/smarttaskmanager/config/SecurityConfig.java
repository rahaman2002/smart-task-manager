package org.example.smarttaskmanager.config;

import lombok.RequiredArgsConstructor;
import org.example.smarttaskmanager.security.CustomUserDetailsService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration // Marks this as a Spring configuration class
@RequiredArgsConstructor // Automatically injects final fields via constructor
public class SecurityConfig {

    // Custom service to load user details from DB (used for username/password login)
    private final CustomUserDetailsService userDetailsService;

    // Custom JWT filter that validates JWT on every request
    private final JwtAuthFilter jwtAuthFilter;

    // Custom handler that runs after successful Google login
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    /**
     * Password encoder bean
     * Used to hash passwords when users register
     * BCrypt is secure and recommended
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * AuthenticationManager bean
     * Used when manually authenticating username/password login
     */
    @Bean
    public AuthenticationManager authManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .userDetailsService(userDetailsService) // Use DB user details
                .passwordEncoder(passwordEncoder())     // Use BCrypt
                .and()
                .build();
    }

    /**
     * Main Security Configuration
     * This controls:
     * - Who can access what
     * - OAuth2 login
     * - JWT filter
     * - Session policy
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // Enable CORS (needed because Angular runs on different port)
                .cors(Customizer.withDefaults())

                // Disable CSRF since we are using stateless JWT authentication
                .csrf(AbstractHttpConfigurer::disable)

                // Authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Allow Google OAuth2 endpoints
                        .requestMatchers(
                                "/oauth2/**",
                                "/login/**",
                                "/api/auth/**" // manual login/register endpoints
                        ).permitAll()

                        // All other endpoints require authentication
                        .anyRequest().authenticated()
                )

                // Enable OAuth2 login (Google login)
                .oauth2Login(oauth2 -> oauth2
                        // After successful Google login, run custom handler
                        // This handler generates JWT and redirects to Angular
                        .successHandler(oAuth2SuccessHandler)
                )

                // Make application stateless (no HTTP sessions stored)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        /**
         * Add custom JWT filter BEFORE Spring's default authentication filter
         * This ensures:
         * - Every request checks for Authorization: Bearer <token>
         * - If valid → user is authenticated
         */
        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS Configuration
     * Needed because:
     * Angular runs on http://localhost:4200
     * Backend runs on http://localhost:8080
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config = new CorsConfiguration();

        // Allow Angular dev server
        config.setAllowedOrigins(List.of("http://localhost:4200"));

        // Allow these HTTP methods
        config.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "DELETE",
                "OPTIONS"
        ));

        // Allow all headers (including Authorization)
        config.setAllowedHeaders(List.of("*"));

        // Allow credentials (important for cookies if used later)
        config.setAllowCredentials(true);

        // Apply this CORS config to all endpoints
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}