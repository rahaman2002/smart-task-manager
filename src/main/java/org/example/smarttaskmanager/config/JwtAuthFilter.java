package org.example.smarttaskmanager.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.smarttaskmanager.security.CustomUserDetailsService;
import org.example.smarttaskmanager.security.JwtTokenProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Custom JWT Authentication Filter
 * Runs **once per request** to validate JWT token if present in the Authorization header
 */
@Component
@RequiredArgsConstructor // Lombok: generates constructor for final fields
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;      // Handles JWT creation & validation
    private final CustomUserDetailsService userDetailsService; // Loads user details from DB

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws java.io.IOException, jakarta.servlet.ServletException {

        String path = request.getServletPath();

        // Skip JWT check for login and register endpoints
        if (path.startsWith("/api/auth/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 1️⃣ Get the Authorization header
        String header = request.getHeader("Authorization");

        // 2️⃣ Check if header starts with "Bearer "
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7); // Remove "Bearer " prefix

            // 3️⃣ Validate the token
            if (jwtTokenProvider.validateToken(token)) {

                // 4️⃣ Extract username from JWT
                String username = jwtTokenProvider.getUsernameFromToken(token);

                // 5️⃣ Load full user details from database
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // 6️⃣ Create an Authentication object and set it in SecurityContext
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        // 7️⃣ Continue the filter chain
        filterChain.doFilter(request, response);
    }
}
