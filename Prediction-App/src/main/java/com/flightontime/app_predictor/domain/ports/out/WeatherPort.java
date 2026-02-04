package com.flightontime.app_predictor.domain.ports.out;

import com.flightontime.app_predictor.application.dto.AirportWeatherDTO;
import java.time.OffsetDateTime;

/**
 * Interfaz WeatherPort.
 */
public interface WeatherPort {
    /**
     * Ejecuta la operación get current weather.
     * @param iata variable de entrada iata.
     * @param instantUtc variable de entrada instantUtc.
     * @return resultado de la operación get current weather.
     */
    AirportWeatherDTO getCurrentWeather(String iata, OffsetDateTime instantUtc);
}
