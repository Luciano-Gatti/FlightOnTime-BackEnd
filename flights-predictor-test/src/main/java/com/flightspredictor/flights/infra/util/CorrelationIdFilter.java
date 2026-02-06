package com.flightspredictor.flights.infra.util;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    public static final String CORRELATION_ID_KEY = "correlationId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        MDC.put(CORRELATION_ID_KEY, correlationId);
        response.setHeader(CORRELATION_ID_HEADER, correlationId);

        try {
            filterChain.doFilter(request, response);
            if (response.getStatus() >= 400) {
                log.error("USECASE_FAIL correlationId={} method={} path={} status={}", correlationId,
                        request.getMethod(), request.getRequestURI(), response.getStatus());
            } else {
                log.info("USECASE_OK correlationId={} method={} path={}", correlationId, request.getMethod(),
                        request.getRequestURI());
            }
        } catch (Exception ex) {
            log.error("USECASE_FAIL correlationId={} method={} path={}", correlationId, request.getMethod(),
                    request.getRequestURI(), ex);
            throw ex;
        } finally {
            int airportLookupCount = AirportLookupTraceContext.getCount();
            log.info("AIRPORT_LOOKUP_COUNT correlationId={} airportLookupCount={}", correlationId, airportLookupCount);
            AirportLookupTraceContext.clear();
            MDC.remove(CORRELATION_ID_KEY);
        }
    }
}
