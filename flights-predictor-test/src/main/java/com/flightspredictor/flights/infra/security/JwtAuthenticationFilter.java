package com.flightspredictor.flights.infra.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization == null || authorization.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!authorization.startsWith("Bearer ")) {
            handleInvalidToken(request, response, "invalid_format");
            return;
        }

        String token = authorization.substring(7).trim();
        try {
            String email = jwtTokenProvider.getEmail(jwtTokenProvider.parseToken(token));
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(buildAuthenticationDetails(userDetails));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                logValidToken(userDetails);
            }
            filterChain.doFilter(request, response);
        } catch (RuntimeException ex) {
            String reason = ex.getClass().getSimpleName();
            if (ex instanceof UsernameNotFoundException) {
                reason = "user_not_found";
            }
            handleInvalidToken(request, response, reason);
        }
    }

    private Map<String, Object> buildAuthenticationDetails(UserDetails userDetails) {
        String roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .reduce((a, b) -> a + "," + b)
                .orElse("");
        Long userId = null;
        if (userDetails instanceof UserPrincipal userPrincipal) {
            userId = userPrincipal.getId();
        }
        return Map.of(
                "userId", userId,
                "email", userDetails.getUsername(),
                "roles", roles
        );
    }

    private void logValidToken(UserDetails userDetails) {
        String correlationId = MDC.get("correlationId");
        String userId = "unknown";
        if (userDetails instanceof UserPrincipal userPrincipal && userPrincipal.getId() != null) {
            userId = userPrincipal.getId().toString();
        }
        log.info("SECURITY_JWT_VALID correlationId={} userId={}", correlationId, userId);
    }

    private void handleInvalidToken(HttpServletRequest request, HttpServletResponse response, String reason)
            throws IOException {
        String correlationId = MDC.get("correlationId");
        log.warn("SECURITY_JWT_INVALID correlationId={} reason={}", correlationId, reason);
        SecurityErrorResponse error = SecurityErrorResponse.unauthorized(
                HttpServletResponse.SC_UNAUTHORIZED,
                "Unauthorized",
                "JWT inválido o expirado",
                request.getRequestURI(),
                correlationId
        );
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(error));
    }
}
