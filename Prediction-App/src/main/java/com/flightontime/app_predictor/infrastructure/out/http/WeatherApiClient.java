package com.flightontime.app_predictor.infrastructure.out.http;

import com.flightontime.app_predictor.application.dto.AirportWeatherDTO;
import com.flightontime.app_predictor.application.services.WeatherService;
import com.flightontime.app_predictor.domain.ports.out.WeatherPort;
import java.time.OffsetDateTime;
import org.springframework.stereotype.Component;

/**
 * Clase WeatherApiClient.
 */
@Component
public class WeatherApiClient implements WeatherPort {
    private final WeatherService weatherService;

    public WeatherApiClient(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    /**
     * Ejecuta la operación get current weather.
     * @param iata variable de entrada iata.
     * @param instantUtc variable de entrada instantUtc.
     * @return resultado de la operación get current weather.
     */
    @Override
    public AirportWeatherDTO getCurrentWeather(String iata, OffsetDateTime instantUtc) {
        return weatherService.getCurrentWeather(iata, instantUtc);
    }
}
