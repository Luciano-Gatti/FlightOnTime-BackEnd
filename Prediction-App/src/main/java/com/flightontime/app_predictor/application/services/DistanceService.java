package com.flightontime.app_predictor.application.services;

import com.flightontime.app_predictor.application.dto.AirportDTO;
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
    private final AirportService airportService;

    /**
     * Construye el servicio de cálculo de distancias.
     *
     * @param airportRepositoryPort repositorio local de aeropuertos.
     * @param airportInfoPort puerto de consulta de aeropuertos externos.
     */
    public DistanceService(
            AirportRepositoryPort airportRepositoryPort, 
            AirportInfoPort airportInfoPort, 
            AirportService airportService) {
        this.airportRepositoryPort = airportRepositoryPort;
        this.airportInfoPort = airportInfoPort;
        this.airportService = airportService;
    }

    
    /**
     * Calcula la distancia entre dos aeropuertos (IATA) usando Haversine.
     *
     * @param originIata IATA de origen.
     * @param destinationIata IATA de destino.
     * @return distancia en kilómetros.
     */
    @Override
    public double calculateDistance(String originIata, String destinationIata) {
        long startMs = UseCaseLogSupport.start(
                log,
                "DistanceService.calculateDistance",
                null,
                "originIata=" + originIata + ", destinationIata=" + destinationIata
        );
        try {
            AirportDTO originDto = airportService.getAirportByIata(originIata);
            AirportDTO destinationDto = airportService.getAirportByIata(destinationIata);

            if (originDto.latitude() == null || originDto.longitude() == null
                    || destinationDto.latitude() == null || destinationDto.longitude() == null) {
                throw new IllegalArgumentException("Airport coordinates are required to calculate distance");
            }

            double distance = haversine(
                    originDto.latitude(), originDto.longitude(),
                    destinationDto.latitude(), destinationDto.longitude()
            );

            UseCaseLogSupport.end(log, "DistanceService.calculateDistance", null, startMs, "distanceKm=" + distance);
            return distance;
        } catch (Exception ex) {
            UseCaseLogSupport.fail(log, "DistanceService.calculateDistance", null, startMs, ex);
            throw ex;
        }
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
