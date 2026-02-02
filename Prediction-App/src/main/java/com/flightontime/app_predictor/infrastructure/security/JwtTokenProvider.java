package com.flightontime.app_predictor.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {
    private static final String ROLES_CLAIM = "roles";
    private static final String EMAIL_CLAIM = "email";

    private final Key signingKey;
    private final long expirationMinutes;

    public JwtTokenProvider(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.expiration-minutes}") long expirationMinutes
    ) {
        this.signingKey = buildSigningKey(secret);
        this.expirationMinutes = expirationMinutes;
    }

    public String generateToken(Long userId, String roles, String email) {
        Objects.requireNonNull(userId, "userId is required");
        String resolvedRoles = roles == null || roles.isBlank() ? "ROLE_USER" : roles;

        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim(ROLES_CLAIM, resolvedRoles)
                .claim(EMAIL_CLAIM, email)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(expirationMinutes * 60)))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);
        String subject = claims.getSubject();
        Collection<GrantedAuthority> authorities = extractAuthorities(claims);
        return new UsernamePasswordAuthenticationToken(subject, token, authorities);
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = parseClaims(token);
        return Long.valueOf(claims.getSubject());
    }

    private Collection<GrantedAuthority> extractAuthorities(Claims claims) {
        Object rolesClaim = claims.get(ROLES_CLAIM);
        if (rolesClaim == null) {
            return Collections.emptyList();
        }
        if (rolesClaim instanceof Collection<?> roleCollection) {
            List<String> roles = roleCollection.stream()
                    .map(Object::toString)
                    .collect(Collectors.toList());
            return toAuthorities(roles);
        }
        String rolesString = rolesClaim.toString();
        List<String> roles = Arrays.stream(rolesString.split(","))
                .map(String::trim)
                .filter(role -> !role.isBlank())
                .collect(Collectors.toList());
        return toAuthorities(roles);
    }

    private Collection<GrantedAuthority> toAuthorities(List<String> roles) {
        return roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    private Key buildSigningKey(String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("JWT secret is required");
        }
        String trimmed = secret.trim();
        byte[] keyBytes;
        if (isBase64(trimmed)) {
            keyBytes = Decoders.BASE64.decode(trimmed);
        } else {
            keyBytes = trimmed.getBytes(StandardCharsets.UTF_8);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private boolean isBase64(String value) {
        try {
            Decoders.BASE64.decode(value);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}
