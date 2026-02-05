package com.flightontime.app_predictor.infrastructure.out.http;

import com.flightontime.app_predictor.application.dto.AirportWeatherDTO;
import com.flightontime.app_predictor.domain.ports.out.WeatherPrimaryProviderPort;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Stub del proveedor primario de clima para desarrollo local.
 */
@Component
@ConditionalOnProperty(name = "providers.stub", havingValue = "true")
public class StubWeatherPrimaryProviderClient implements WeatherPrimaryProviderPort {

    @Override
    public AirportWeatherDTO fetchCurrentWeather(double lat, double lon) {
        return new AirportWeatherDTO(
                21.0,
                12.0,
                10000.0,
                false,
                OffsetDateTime.now(ZoneOffset.UTC)
        );
    }
}
