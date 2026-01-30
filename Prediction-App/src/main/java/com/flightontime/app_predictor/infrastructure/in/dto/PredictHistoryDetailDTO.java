package com.flightontime.app_predictor.infrastructure.in.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.OffsetDateTime;
import java.util.List;

public record PredictHistoryDetailDTO(
        Long requestId,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        OffsetDateTime flightDate,
        String carrier,
        String origin,
        String destination,
        String flightNumber,
        long uniqueUsersCount,
        List<PredictHistoryPredictionDTO> predictions
) {
}
