package com.flightontime.app_predictor.domain.ports.out;

import com.flightontime.app_predictor.application.dto.AirportWeatherDTO;

/**
 * Puerto para proveedor fallback de clima.
 */
public interface WeatherFallbackProviderPort {
    /**
     * Obtiene clima actual para coordenadas.
     *
     * @param lat latitud.
     * @param lon longitud.
     * @return clima actual.
     */
    AirportWeatherDTO fetchCurrentWeather(double lat, double lon);
}
