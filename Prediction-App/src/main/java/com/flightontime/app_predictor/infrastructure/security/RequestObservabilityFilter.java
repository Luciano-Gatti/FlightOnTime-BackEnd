package com.flightontime.app_predictor.infrastructure.security;

import com.flightontime.app_predictor.infrastructure.config.CorrelationIdFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filtro de observabilidad por request.
 */
@Component
public class RequestObservabilityFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestObservabilityFilter.class);

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        long start = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
            logInfo(request, response, System.currentTimeMillis() - start);
        } catch (Exception ex) {
            long durationMs = System.currentTimeMillis() - start;
            LOGGER.error(
                    "request.error correlationId={} method={} path={} status={} durationMs={} userId={}",
                    correlationId(),
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    durationMs,
                    resolveUserId(),
                    ex
            );
            throw ex;
        }
    }

    private void logInfo(HttpServletRequest request, HttpServletResponse response, long durationMs) {
        LOGGER.info(
                "request correlationId={} method={} path={} status={} durationMs={} userId={}",
                correlationId(),
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus(),
                durationMs,
                resolveUserId()
        );
    }

    private String correlationId() {
        return MDC.get(CorrelationIdFilter.MDC_KEY);
    }

    private String resolveUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof String value) {
            if (value.isBlank() || "anonymousUser".equalsIgnoreCase(value)) {
                return null;
            }
            return value;
        }

        return null;
    }
}
