package com.flightontime.app_predictor.application.services;

import com.flightontime.app_predictor.infrastructure.config.CorrelationIdFilter;
import org.slf4j.Logger;
import org.slf4j.MDC;

/**
 * Utilidad para logs estructurados de casos de uso.
 */
public final class UseCaseLogSupport {

    private UseCaseLogSupport() {
    }

    public static long start(Logger logger, String useCase, Long userId, String inputs) {
        long startMs = System.currentTimeMillis();
        logger.info(
                "USECASE_START useCase={} correlationId={} userId={} inputs={}",
                useCase,
                correlationId(),
                userId,
                sanitize(inputs)
        );
        return startMs;
    }

    public static void end(Logger logger, String useCase, Long userId, long startMs, String result) {
        logger.info(
                "USECASE_END useCase={} correlationId={} userId={} durationMs={} result={}",
                useCase,
                correlationId(),
                userId,
                System.currentTimeMillis() - startMs,
                sanitize(result)
        );
    }

    public static void fail(Logger logger, String useCase, Long userId, long startMs, Exception ex) {
        logger.error(
                "USECASE_FAIL useCase={} correlationId={} userId={} durationMs={} exceptionType={} exceptionMessage={}",
                useCase,
                correlationId(),
                userId,
                System.currentTimeMillis() - startMs,
                ex != null ? ex.getClass().getSimpleName() : null,
                ex != null ? sanitize(ex.getMessage()) : null,
                ex
        );
    }

    private static String correlationId() {
        return MDC.get(CorrelationIdFilter.MDC_KEY);
    }

    private static String sanitize(String value) {
        if (value == null) {
            return null;
        }
        return value.replace("\n", " ").replace("\r", " ").trim();
    }
}
