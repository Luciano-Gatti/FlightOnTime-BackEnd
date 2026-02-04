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

/**
 * Clase JwtTokenProvider.
 */
@Component
public class JwtTokenProvider {
    private static final String ROLES_CLAIM = "roles";
    private static final String EMAIL_CLAIM = "email";

    private final Key signingKey;
    private final long expirationMinutes;
    private final boolean secretBase64;

    public JwtTokenProvider(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.expiration-minutes}") long expirationMinutes,
            @Value("${security.jwt.secret-base64:false}") boolean secretBase64
    ) {
        this.secretBase64 = secretBase64;
        this.signingKey = buildSigningKey(secret);
        this.expirationMinutes = expirationMinutes;
    }

    /**
     * Ejecuta la operación generate token.
     * @param userId variable de entrada userId.
     * @param roles variable de entrada roles.
     * @param email variable de entrada email.
     * @return resultado de la operación generate token.
     */

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

    /**
     * Ejecuta la operación validate token.
     * @param token variable de entrada token.
     * @return resultado de la operación validate token.
     */

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Ejecuta la operación get authentication.
     * @param token variable de entrada token.
     * @return resultado de la operación get authentication.
     */

    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);
        String subject = claims.getSubject();
        Collection<GrantedAuthority> authorities = extractAuthorities(claims);
        return new UsernamePasswordAuthenticationToken(subject, token, authorities);
    }

    /**
     * Ejecuta la operación get user id from token.
     * @param token variable de entrada token.
     * @return resultado de la operación get user id from token.
     */

    public Long getUserIdFromToken(String token) {
        Claims claims = parseClaims(token);
        return Long.valueOf(claims.getSubject());
    }

    /**
     * Ejecuta la operación extract authorities.
     * @param claims variable de entrada claims.
     * @return resultado de la operación extract authorities.
     */

    private Collection<GrantedAuthority> extractAuthorities(Claims claims) {
        Object rolesClaim = claims.get(ROLES_CLAIM);
        if (rolesClaim == null) {
            return Collections.emptyList();
        }
        if (rolesClaim instanceof Collection<?> roleCollection) {
            List<String> roles = roleCollection.stream()
                    .map(Object::toString)
                    .collect(Collectors.toList());
            /**
             * Ejecuta la operación to authorities.
             * @param roles variable de entrada roles.
             * @return resultado de la operación to authorities.
             */
            return toAuthorities(roles);
        }
        String rolesString = rolesClaim.toString();
        List<String> roles = Arrays.stream(rolesString.split(","))
                .map(String::trim)
                .filter(role -> !role.isBlank())
                .collect(Collectors.toList());
        /**
         * Ejecuta la operación to authorities.
         * @param roles variable de entrada roles.
         * @return resultado de la operación to authorities.
         */
        return toAuthorities(roles);
    }

    /**
     * Ejecuta la operación to authorities.
     * @param roles variable de entrada roles.
     * @return resultado de la operación to authorities.
     */

    private Collection<GrantedAuthority> toAuthorities(List<String> roles) {
        return roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    /**
     * Ejecuta la operación build signing key.
     * @param secret variable de entrada secret.
     * @return resultado de la operación build signing key.
     */

    private Key buildSigningKey(String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("JWT secret is required");
        }
        String trimmed = secret.trim();
        byte[] keyBytes = secretBase64
                ? Decoders.BASE64.decode(trimmed)
                : trimmed.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException(
                    "JWT secret too short: must be at least 32 bytes (256 bits) for HS256. Current: "
                            + keyBytes.length + " bytes."
            );
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Ejecuta la operación parse claims.
     * @param token variable de entrada token.
     * @return resultado de la operación parse claims.
     */

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

}
