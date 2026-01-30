package com.flightontime.app_predictor.domain.ports.in;

public interface DistanceUseCase {
    double calculateDistance(String originIata, String destinationIata);
}
