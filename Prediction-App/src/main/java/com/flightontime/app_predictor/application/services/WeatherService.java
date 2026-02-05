package com.flightontime.app_predictor.application.services;

import com.flightontime.app_predictor.application.dto.AirportWeatherDTO;
import com.flightontime.app_predictor.application.exception.WeatherProviderException;
import com.flightontime.app_predictor.domain.model.Airport;
import com.flightontime.app_predictor.domain.ports.out.AirportInfoPort;
import com.flightontime.app_predictor.domain.ports.out.AirportRepositoryPort;
import com.flightontime.app_predictor.domain.ports.out.WeatherFallbackProviderPort;
import com.flightontime.app_predictor.domain.ports.out.WeatherPrimaryProviderPort;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Clase WeatherService.
 */
@Service
public class WeatherService {
    private static final Logger log = LoggerFactory.getLogger(WeatherService.class);
    private final WeatherPrimaryProviderPort weatherPrimaryProviderPort;
    private final WeatherFallbackProviderPort weatherFallbackProviderPort;
    private final AirportRepositoryPort airportRepositoryPort;
    private final AirportInfoPort airportInfoPort;
    private final Duration cacheTtl;
    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public WeatherService(
            WeatherPrimaryProviderPort weatherPrimaryProviderPort,
            WeatherFallbackProviderPort weatherFallbackProviderPort,
            AirportRepositoryPort airportRepositoryPort,
            AirportInfoPort airportInfoPort,
            @Value("${weather.cache.ttl}") Duration cacheTtl
    ) {
        this.weatherPrimaryProviderPort = weatherPrimaryProviderPort;
        this.weatherFallbackProviderPort = weatherFallbackProviderPort;
        this.airportRepositoryPort = airportRepositoryPort;
        this.airportInfoPort = airportInfoPort;
        this.cacheTtl = cacheTtl;
    }

    /**
     * Obtiene el clima actual para un aeropuerto en un instante dado.
     *
     * @param iata IATA del aeropuerto.
     * @param instantUtc instante de consulta en UTC.
     * @return DTO con información meteorológica.
     */
    public AirportWeatherDTO getCurrentWeather(String iata, OffsetDateTime instantUtc) {
        long startMs = UseCaseLogSupport.start(
                log,
                "WeatherService.getCurrentWeather",
                null,
                "iata=" + iata + ", instantUtc=" + instantUtc
        );
        try {
            String normalizedIata = iata.toUpperCase(Locale.ROOT);
            OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
            CacheEntry cached = cache.get(normalizedIata);
            if (cached != null && now.isBefore(cached.expiresAt())) {
                log.info("Weather cache hit for iata={}", normalizedIata);
                UseCaseLogSupport.end(log, "WeatherService.getCurrentWeather", null, startMs, "iata=" + normalizedIata + ", source=cache");
                return cached.weather();
            }
            log.info("Weather lookup start iata={} instantUtc={}", normalizedIata, instantUtc);
            Airport airport = resolveAirport(normalizedIata);

            try {
            log.info("Calling OpenMeteo weather for iata={} lat={} lon={}",
                    airport.airportIata(), airport.latitude(), airport.longitude());
                AirportWeatherDTO dto = weatherPrimaryProviderPort.fetchCurrentWeather(airport.latitude(), airport.longitude());
                cache.put(normalizedIata, new CacheEntry(dto, now.plus(cacheTtl)));
                UseCaseLogSupport.end(log, "WeatherService.getCurrentWeather", null, startMs, "iata=" + normalizedIata + ", source=primary");
                return dto;
        } catch (RuntimeException primaryError) {
            try {
                log.info("Calling fallback weather for iata={} lat={} lon={}",
                        airport.airportIata(), airport.latitude(), airport.longitude());
                    AirportWeatherDTO dto = weatherFallbackProviderPort.fetchCurrentWeather(airport.latitude(), airport.longitude());
                    cache.put(normalizedIata, new CacheEntry(dto, now.plus(cacheTtl)));
                    UseCaseLogSupport.end(log, "WeatherService.getCurrentWeather", null, startMs, "iata=" + normalizedIata + ", source=fallback");
                    return dto;
            } catch (RuntimeException fallbackError) {
                WeatherProviderException providerException = new WeatherProviderException(
                        "Weather provider error for IATA " + normalizedIata,
                        primaryError
                );
                providerException.addSuppressed(fallbackError);
                    throw providerException;
                }
            }
        } catch (Exception ex) {
            UseCaseLogSupport.fail(log, "WeatherService.getCurrentWeather", null, startMs, ex);
            throw ex;
        }
    }

    private Airport resolveAirport(String normalizedIata) {
        Airport airport = airportRepositoryPort.findByIata(normalizedIata)
                .orElseGet(() -> airportInfoPort.findByIata(normalizedIata)
                        .map(this::storeAirport)
                        .orElseThrow(() -> new IllegalArgumentException("Airport not found: " + normalizedIata)));
        if (airport.latitude() == null || airport.longitude() == null) {
            throw new IllegalArgumentException("Airport coordinates are required for weather data: " + normalizedIata);
        }
        return airport;
    }

    private Airport storeAirport(Airport airport) {
        log.info("Storing airport from external source for weather iata={}", airport.airportIata());
        airportRepositoryPort.saveAll(List.of(airport));
        return airport;
    }

    private record CacheEntry(AirportWeatherDTO weather, OffsetDateTime expiresAt) {
    }
}
