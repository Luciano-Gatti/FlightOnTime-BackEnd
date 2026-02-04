package com.flightontime.app_predictor.domain.ports.in;

/**
 * Interfaz DistanceUseCase.
 */
public interface DistanceUseCase {
    /**
     * Ejecuta la operación calculate distance.
     * @param originIata variable de entrada originIata.
     * @param destinationIata variable de entrada destinationIata.
     * @return resultado de la operación calculate distance.
     */
    double calculateDistance(String originIata, String destinationIata);
}
