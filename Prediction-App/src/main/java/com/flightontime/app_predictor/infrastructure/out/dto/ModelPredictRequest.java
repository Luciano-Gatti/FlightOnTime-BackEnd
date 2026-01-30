package com.flightontime.app_predictor.infrastructure.out.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.OffsetDateTime;

public record ModelPredictRequest(
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        OffsetDateTime flDate,
        String carrier,
        String origin,
        String dest,
        String flightNumber,
        double distance
) {
}
