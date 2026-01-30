package com.flightontime.app_predictor.infrastructure.out.adapters;

public class WeatherProviderException extends RuntimeException {
    public WeatherProviderException(String message, Throwable cause) {
        super(message, cause);
    }
}
