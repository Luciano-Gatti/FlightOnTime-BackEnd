package com.flightontime.app_predictor.application.exception;

/**
 * Clase WeatherProviderException.
 */
public class WeatherProviderException extends RuntimeException {
    /**
     * Construye la excepción de proveedor de clima.
     *
     * @param message variable de entrada message.
     * @param cause variable de entrada cause.
     */
    public WeatherProviderException(String message, Throwable cause) {
        super(message, cause);
    }
}
