package com.flightspredictor.flights.infra.util;

import com.flightspredictor.flights.domain.service.airports.AirportService;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class AirportRepositoryLookupAspect {

    private static final Set<String> LOOKUP_PREFIXES = Set.of("find", "exists", "get");

    private final boolean airportLookupTraceEnabled;
    private final boolean airportRepoTraceEnabled;

    public AirportRepositoryLookupAspect(
            @Value("${app.debug.airportLookupTrace:false}") boolean airportLookupTraceEnabled,
            @Value("${app.debug.airportRepoTrace:false}") boolean airportRepoTraceEnabled) {
        this.airportLookupTraceEnabled = airportLookupTraceEnabled;
        this.airportRepoTraceEnabled = airportRepoTraceEnabled;
    }

    @Around("execution(* com.flightspredictor.flights.domain.repository.AirportRepository.*(..))")
    public Object trackRepositoryLookup(ProceedingJoinPoint joinPoint) throws Throwable {
        if (airportRepoTraceEnabled && shouldTraceRepoCall(joinPoint)) {
            String correlationId = MDC.get("correlationId");
            String caller = resolveCaller();
            String iata = resolveIata(joinPoint);
            log.info("AIRPORT_REPO_CALL correlationId={} method={} caller={} iata={}",
                    correlationId,
                    joinPoint.getSignature().getName(),
                    caller,
                    iata);
        }
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

    private boolean shouldTraceRepoCall(ProceedingJoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        return methodName.equals("findByAirportIata")
                || methodName.startsWith("existsByAirportIata");
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

    private String resolveCaller() {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stack) {
            String className = element.getClassName();
            if (shouldSkipFrame(className)) {
                continue;
            }
            if (className.startsWith("com.flightspredictor.")) {
                return className + "#" + element.getMethodName() + ":" + element.getLineNumber();
            }
        }
        return "UNKNOWN";
    }

    private boolean shouldSkipFrame(String className) {
        return className.equals(AirportRepositoryLookupAspect.class.getName())
                || className.equals(AirportService.class.getName())
                || className.startsWith("com.flightspredictor.flights.domain.repository")
                || className.startsWith("java.")
                || className.startsWith("javax.")
                || className.startsWith("jakarta.")
                || className.startsWith("sun.")
                || className.startsWith("jdk.")
                || className.startsWith("org.springframework.")
                || className.startsWith("org.hibernate.")
                || className.startsWith("com.sun.proxy")
                || className.startsWith("net.sf.cglib")
                || className.startsWith("org.aopalliance")
                || className.startsWith("org.aspectj.");
    }

    private String resolveIata(ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args.length == 0 || !(args[0] instanceof String)) {
            return "UNKNOWN";
        }
        String rawIata = (String) args[0];
        return normalizeIata(rawIata);
    }

    private String normalizeIata(String iata) {
        if (iata == null) {
            return "NULL";
        }
        return iata.trim().toUpperCase();
    }
}
