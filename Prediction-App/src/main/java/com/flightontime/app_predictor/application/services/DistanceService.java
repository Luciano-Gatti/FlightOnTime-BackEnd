package com.flightontime.app_predictor.application.services;

import com.flightontime.app_predictor.domain.model.Airport;
import com.flightontime.app_predictor.domain.ports.in.DistanceUseCase;
import com.flightontime.app_predictor.domain.ports.out.AirportInfoPort;
import com.flightontime.app_predictor.domain.ports.out.AirportRepositoryPort;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Clase DistanceService.
 */
@Service
public class DistanceService implements DistanceUseCase {
    private static final double EARTH_RADIUS_KM = 6371.0088;
    private static final Logger log = LoggerFactory.getLogger(DistanceService.class);

    private final AirportRepositoryPort airportRepositoryPort;
    private final AirportInfoPort airportInfoPort;

    /**
     * Construye el servicio de cálculo de distancias.
     *
     * @param airportRepositoryPort repositorio local de aeropuertos.
     * @param airportInfoPort puerto de consulta de aeropuertos externos.
     */
    public DistanceService(AirportRepositoryPort airportRepositoryPort, AirportInfoPort airportInfoPort) {
        this.airportRepositoryPort = airportRepositoryPort;
        this.airportInfoPort = airportInfoPort;
    }

    @Override
    /**
     * Calcula la distancia entre dos aeropuertos (IATA) usando Haversine.
     *
     * @param originIata IATA de origen.
     * @param destinationIata IATA de destino.
     * @return distancia en kilómetros.
     */
    public double calculateDistance(String originIata, String destinationIata) {
        long startMs = UseCaseLogSupport.start(
                log,
                "DistanceService.calculateDistance",
                null,
                "originIata=" + originIata + ", destinationIata=" + destinationIata
        );
        try {
            String normalizedOrigin = normalizeIata(originIata);
            String normalizedDestination = normalizeIata(destinationIata);
            Airport origin = resolveAirport(normalizedOrigin);
            Airport destination = resolveAirport(normalizedDestination);
            if (origin.latitude() == null || origin.longitude() == null
                    || destination.latitude() == null || destination.longitude() == null) {
                throw new IllegalArgumentException("Airport coordinates are required to calculate distance");
            }
            double distance = haversine(origin.latitude(), origin.longitude(), destination.latitude(), destination.longitude());
            UseCaseLogSupport.end(
                    log,
                    "DistanceService.calculateDistance",
                    null,
                    startMs,
                    "distanceKm=" + distance
            );
            return distance;
        } catch (Exception ex) {
            UseCaseLogSupport.fail(log, "DistanceService.calculateDistance", null, startMs, ex);
            throw ex;
        }
    }

    /**
     * Resuelve un aeropuerto desde el repositorio local o fuente externa.
     *
     * @param normalizedIata IATA normalizado (trim y upper).
     * @return aeropuerto encontrado o persistido.
     */
    private Airport resolveAirport(String normalizedIata) {
        return airportRepositoryPort.findByIata(normalizedIata)
                .orElseGet(() -> airportInfoPort.findByIata(normalizedIata)
                        .map(this::storeAirport)
                        .orElseThrow(() -> {
                            log.warn("Airport not found while calculating distance: {}", normalizedIata);
                            return new IllegalArgumentException("Airport not found: " + normalizedIata);
                        }));
    }

    /**
     * Persiste un aeropuerto obtenido desde una fuente externa.
     *
     * @param airport aeropuerto a almacenar.
     * @return aeropuerto persistido (sin modificación de datos).
     */
    private Airport storeAirport(Airport airport) {
        log.info("Storing airport from external source for distance calculation: {}", airport.airportIata());
        return airportRepositoryPort.save(airport);
    }

    /**
     * Normaliza el IATA a mayúsculas y sin espacios.
     *
     * @param airportIata IATA original.
     * @return IATA normalizado o null si el valor era null.
     */
    private String normalizeIata(String airportIata) {
        if (airportIata == null || airportIata.isBlank()) {
            throw new IllegalArgumentException("iata is required");
        }
        String normalized = airportIata.trim().toUpperCase(Locale.ROOT);
        if (normalized.length() != 3) {
            throw new IllegalArgumentException("iata must be length 3");
        }
        return normalized;
    }

    /**
     * Aplica la fórmula de Haversine para distancia entre coordenadas.
     *
     * @param originLat latitud de origen.
     * @param originLon longitud de origen.
     * @param destinationLat latitud de destino.
     * @param destinationLon longitud de destino.
     * @return distancia en kilómetros.
     */
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
