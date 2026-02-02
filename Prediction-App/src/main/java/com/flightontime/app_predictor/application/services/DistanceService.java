package com.flightontime.app_predictor.application.services;

import com.flightontime.app_predictor.domain.model.Airport;
import com.flightontime.app_predictor.domain.ports.in.DistanceUseCase;
import com.flightontime.app_predictor.domain.ports.out.AirportInfoPort;
import com.flightontime.app_predictor.domain.ports.out.AirportRepositoryPort;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DistanceService implements DistanceUseCase {
    private static final double EARTH_RADIUS_KM = 6371.0088;
    private static final Logger log = LoggerFactory.getLogger(DistanceService.class);

    private final AirportRepositoryPort airportRepositoryPort;
    private final AirportInfoPort airportInfoPort;

    public DistanceService(AirportRepositoryPort airportRepositoryPort, AirportInfoPort airportInfoPort) {
        this.airportRepositoryPort = airportRepositoryPort;
        this.airportInfoPort = airportInfoPort;
    }

    @Override
    public double calculateDistance(String originIata, String destinationIata) {
        String normalizedOrigin = normalizeIata(originIata);
        String normalizedDestination = normalizeIata(destinationIata);
        Airport origin = resolveAirport(normalizedOrigin);
        Airport destination = resolveAirport(normalizedDestination);
        if (origin.latitude() == null || origin.longitude() == null
                || destination.latitude() == null || destination.longitude() == null) {
            throw new IllegalArgumentException("Airport coordinates are required to calculate distance");
        }
        return haversine(origin.latitude(), origin.longitude(), destination.latitude(), destination.longitude());
    }

    private Airport resolveAirport(String normalizedIata) {
        return airportRepositoryPort.findByIata(normalizedIata)
                .orElseGet(() -> airportInfoPort.findByIata(normalizedIata)
                        .map(this::storeAirport)
                        .orElseThrow(() -> {
                            log.warn("Airport not found while calculating distance: {}", normalizedIata);
                            return new IllegalArgumentException("Airport not found: " + normalizedIata);
                        }));
    }

    private Airport storeAirport(Airport airport) {
        log.info("Storing airport from external source for distance calculation: {}", airport.airportIata());
        airportRepositoryPort.saveAll(List.of(airport));
        return airport;
    }

    private String normalizeIata(String airportIata) {
        if (airportIata == null) {
            return null;
        }
        return airportIata.trim().toUpperCase(Locale.ROOT);
    }

    private double haversine(double originLat, double originLon, double destinationLat, double destinationLon) {
        double originLatRad = Math.toRadians(originLat);
        double destinationLatRad = Math.toRadians(destinationLat);
        double deltaLat = Math.toRadians(destinationLat - originLat);
        double deltaLon = Math.toRadians(destinationLon - originLon);
        double sinLat = Math.sin(deltaLat / 2);
        double sinLon = Math.sin(deltaLon / 2);
        double a = sinLat * sinLat
                + Math.cos(originLatRad) * Math.cos(destinationLatRad) * sinLon * sinLon;
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }
}
