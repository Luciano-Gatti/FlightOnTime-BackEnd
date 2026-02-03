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

    /**
     * Construye el servicio de consulta meteorológica.
     *
     * @param weatherPort puerto de acceso a proveedor de clima.
     */
    public WeatherService(WeatherPort weatherPort) {
        this.weatherPort = weatherPort;
    }

    /**
     * Obtiene el clima actual para un aeropuerto en un instante dado.
     *
     * @param iata IATA del aeropuerto.
     * @param instantUtc instante de consulta en UTC.
     * @return DTO con información meteorológica.
     */
    public AirportWeatherDTO getCurrentWeather(String iata, OffsetDateTime instantUtc) {
        return weatherPort.getCurrentWeather(iata, instantUtc);
    }
}
