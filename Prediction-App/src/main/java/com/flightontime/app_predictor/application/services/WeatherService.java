package com.flightontime.app_predictor.application.services;

import com.flightontime.app_predictor.application.dto.AirportWeatherDTO;
import com.flightontime.app_predictor.domain.ports.out.WeatherPort;
import java.time.OffsetDateTime;
import org.springframework.stereotype.Service;

/**
 * Clase WeatherService.
 */
@Service
public class WeatherService {
    private final WeatherPort weatherPort;

    public WeatherService(WeatherPort weatherPort) {
        this.weatherPort = weatherPort;
    }

    public AirportWeatherDTO getCurrentWeather(String iata, OffsetDateTime instantUtc) {
        return weatherPort.getCurrentWeather(iata, instantUtc);
    }
}
