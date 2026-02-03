package com.flightontime.app_predictor.domain.ports.out;

import com.flightontime.app_predictor.application.dto.AirportWeatherDTO;
import java.time.OffsetDateTime;

/**
 * Interfaz WeatherPort.
 */
public interface WeatherPort {
    AirportWeatherDTO getCurrentWeather(String iata, OffsetDateTime instantUtc);
}
