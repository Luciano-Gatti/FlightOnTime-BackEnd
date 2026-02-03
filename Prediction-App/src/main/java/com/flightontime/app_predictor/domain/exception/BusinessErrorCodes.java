package com.flightontime.app_predictor.domain.exception;

public final class BusinessErrorCodes {

    private BusinessErrorCodes() {
        // Evita la instanciación
    }

    public static final String INVALID_ROUTE = "INVALID_ROUTE";
    public static final String INVALID_IATA = "INVALID_IATA_CODE";
    public static final String INVALID_AIRLINE_CODE = "INVALID_AIRLINE_CODE";
}
