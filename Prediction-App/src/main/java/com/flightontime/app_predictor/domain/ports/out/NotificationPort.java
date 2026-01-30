package com.flightontime.app_predictor.domain.ports.out;

import com.flightontime.app_predictor.domain.model.FlightRequest;
import com.flightontime.app_predictor.domain.model.Prediction;

public interface NotificationPort {
    void sendT12hStatusChange(
            Long userId,
            FlightRequest request,
            Prediction baseline,
            Prediction current
    );
}
