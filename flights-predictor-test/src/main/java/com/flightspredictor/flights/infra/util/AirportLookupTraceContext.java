package com.flightspredictor.flights.infra.util;

import org.slf4j.MDC;

public final class AirportLookupTraceContext {

    public static final String AIRPORT_LOOKUP_COUNT_KEY = "airportLookupCount";
    private static final ThreadLocal<Integer> LOOKUP_COUNT = ThreadLocal.withInitial(() -> 0);

    private AirportLookupTraceContext() {
    }

    public static int incrementAndGet() {
        int nextCount = LOOKUP_COUNT.get() + 1;
        LOOKUP_COUNT.set(nextCount);
        MDC.put(AIRPORT_LOOKUP_COUNT_KEY, String.valueOf(nextCount));
        return nextCount;
    }

    public static int getCount() {
        return LOOKUP_COUNT.get();
    }

    public static void clear() {
        LOOKUP_COUNT.remove();
        MDC.remove(AIRPORT_LOOKUP_COUNT_KEY);
    }
}
