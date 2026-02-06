package com.flightspredictor.flights.infra.util;

import com.flightspredictor.flights.domain.entities.FlightPrediction;
import com.flightspredictor.flights.domain.entities.FlightRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class RepositorySaveLoggingAspect {

    @Around("execution(* com.flightspredictor.flights.domain.repository.FlightRequestRepository.save(..))")
    public Object logFlightRequestSave(ProceedingJoinPoint joinPoint) throws Throwable {
        logSave(joinPoint, "FlightRequest");
        return joinPoint.proceed();
    }

    @Around("execution(* com.flightspredictor.flights.domain.repository.FlightPredictionRepository.save(..))")
    public Object logFlightPredictionSave(ProceedingJoinPoint joinPoint) throws Throwable {
        logSave(joinPoint, "FlightPrediction");
        return joinPoint.proceed();
    }

    private void logSave(ProceedingJoinPoint joinPoint, String entityName) {
        String correlationId = MDC.get("correlationId");
        String idValue = resolveEntityId(joinPoint.getArgs());
        log.info("REPO_SAVE entity={} id={} correlationId={}", entityName, idValue, correlationId);
    }

    private String resolveEntityId(Object[] args) {
        if (args == null || args.length == 0) {
            return "null";
        }
        Object entity = args[0];
        if (entity instanceof FlightRequest request) {
            return request.getId() == null ? "null" : request.getId().toString();
        }
        if (entity instanceof FlightPrediction prediction) {
            return prediction.getId() == null ? "null" : prediction.getId().toString();
        }
        return "null";
    }
}
