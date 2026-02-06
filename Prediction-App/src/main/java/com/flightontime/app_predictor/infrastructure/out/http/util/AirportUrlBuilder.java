package com.flightontime.app_predictor.infrastructure.out.http.util;

/**
 * Utilidad para construir la URL del proveedor de aeropuertos.
 */
public final class AirportUrlBuilder {
    // En el proyecto test la base URL incluye /airports/ y se concatena /Iata/{iata}?withRunways=false&withTime=false.
    private static final String BASE_URL = "https://prod.api.market/api/v1/aedbx/aerodatabox/airports/";
    private static final String CODE_TYPE_DEFAULT = "Iata";

    private AirportUrlBuilder() {
    }

    public static String buildAirportUrl(String airportIata) {
        return String.format(
                "%s/%s/%s?withRunways=false&withTime=false",
                BASE_URL,
                CODE_TYPE_DEFAULT,
                airportIata.toUpperCase()
        );
    }
}
