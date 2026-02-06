package com.flightspredictor.flights.infra.util;

import com.flightspredictor.flights.domain.service.airports.AirportService;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class AirportRepositoryLookupAspect {

    private static final Set<String> LOOKUP_PREFIXES = Set.of("find", "exists", "get");

    private final boolean airportLookupTraceEnabled;

    public AirportRepositoryLookupAspect(
            @Value("${app.debug.airportLookupTrace:false}") boolean airportLookupTraceEnabled) {
        this.airportLookupTraceEnabled = airportLookupTraceEnabled;
    }

    @Around("execution(* com.flightspredictor.flights.domain.repository.AirportRepository.*(..))")
    public Object trackRepositoryLookup(ProceedingJoinPoint joinPoint) throws Throwable {
        if (shouldTrack(joinPoint) && !calledFromAirportService()) {
            int lookupCount = AirportLookupTraceContext.incrementAndGet();
            if (airportLookupTraceEnabled) {
                log.info("AIRPORT_LOOKUP_TRACE repositoryCall={} airportLookupCount={}",
                        joinPoint.getSignature().getName(),
                        lookupCount);
            }
        }
        return joinPoint.proceed();
    }

    private boolean shouldTrack(ProceedingJoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        return LOOKUP_PREFIXES.stream().anyMatch(methodName::startsWith);
    }

    private boolean calledFromAirportService() {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stack) {
            if (element.getClassName().equals(AirportService.class.getName())) {
                return true;
            }
        }
        return false;
    }
}
