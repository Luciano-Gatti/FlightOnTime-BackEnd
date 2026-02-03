package com.flightontime.app_predictor.domain.ports.in;

/**
 * Interfaz DistanceUseCase.
 */
public interface DistanceUseCase {
    double calculateDistance(String originIata, String destinationIata);
}
