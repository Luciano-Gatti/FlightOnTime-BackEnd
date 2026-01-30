package com.flightontime.app_predictor.application.services;

import com.flightontime.app_predictor.domain.model.Airport;
import com.flightontime.app_predictor.domain.ports.in.DistanceUseCase;
import com.flightontime.app_predictor.domain.ports.out.AirportRepositoryPort;
import org.springframework.stereotype.Service;

@Service
public class DistanceService implements DistanceUseCase {
    private static final double EARTH_RADIUS_KM = 6371.0088;

    private final AirportRepositoryPort airportRepositoryPort;

    public DistanceService(AirportRepositoryPort airportRepositoryPort) {
        this.airportRepositoryPort = airportRepositoryPort;
    }

    @Override
    public double calculateDistance(String originIata, String destinationIata) {
        Airport origin = airportRepositoryPort.findByIata(originIata)
                .orElseThrow(() -> new IllegalArgumentException("Airport not found: " + originIata));
        Airport destination = airportRepositoryPort.findByIata(destinationIata)
                .orElseThrow(() -> new IllegalArgumentException("Airport not found: " + destinationIata));
        if (origin.latitude() == null || origin.longitude() == null
                || destination.latitude() == null || destination.longitude() == null) {
            throw new IllegalArgumentException("Airport coordinates are required to calculate distance");
        }
        return haversine(origin.latitude(), origin.longitude(), destination.latitude(), destination.longitude());
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
