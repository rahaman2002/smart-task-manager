package org.example.smarttaskmanager.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.example.smarttaskmanager.model.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Set;
import java.security.Key;

@Component
public class JwtTokenProvider {

    private final Key secretKey;
    private final long expirationMs;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String jwtSecret,
            @Value("${jwt.expiration}") long jwtExpirationMs
    ) {
        this.secretKey = Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(jwtSecret)
        );
        this.expirationMs = jwtExpirationMs;
    }

    // Generate token for user
    public String generateToken(String username, Set<Role> roles) {
        String rolesString = roles.stream()
                .map(Role::name)
                .reduce((r1, r2) -> r1 + "," + r2)
                .orElse("");

        return Jwts.builder()
                .setSubject(username)
                .claim("roles", rolesString)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();
    }

    // Get username from token
    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // Get roles from token
    public Set<String> getRolesFromToken(String token) {
        String rolesStr = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("roles", String.class);
        if (rolesStr == null || rolesStr.isEmpty()) return Set.of();
        return Set.of(rolesStr.split(","));
    }

    // Validate token
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
