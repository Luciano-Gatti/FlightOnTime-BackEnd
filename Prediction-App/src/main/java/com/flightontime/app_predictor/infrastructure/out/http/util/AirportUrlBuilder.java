package com.flightontime.app_predictor.infrastructure.out.http.util;

/**
 * Utilidad para construir la URL del proveedor de aeropuertos.
 */
public final class AirportUrlBuilder {
    private static final String CODE_TYPE_DEFAULT = "iata";

    private AirportUrlBuilder() {
    }

    public static String buildAirportUrl(String airportIata) {
        return String.format(
                "/airports/%s/%s?withRunways=false&withTime=false",
                CODE_TYPE_DEFAULT,
                airportIata.toUpperCase()
        );
    }
}
