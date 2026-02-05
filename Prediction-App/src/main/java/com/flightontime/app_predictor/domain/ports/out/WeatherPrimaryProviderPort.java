package com.flightontime.app_predictor.domain.ports.out;

import com.flightontime.app_predictor.application.dto.AirportWeatherDTO;

/**
 * Puerto para proveedor primario de clima.
 */
public interface WeatherPrimaryProviderPort {
    /**
     * Obtiene clima actual para coordenadas.
     *
     * @param lat latitud.
     * @param lon longitud.
     * @return clima actual.
     */
    AirportWeatherDTO fetchCurrentWeather(double lat, double lon);
}
