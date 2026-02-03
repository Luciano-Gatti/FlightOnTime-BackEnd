package com.flightontime.app_predictor.infrastructure.out.adapters;

/**
 * Clase WeatherProviderException.
 */
public class WeatherProviderException extends RuntimeException {
    public WeatherProviderException(String message, Throwable cause) {
        super(message, cause);
    }
}
