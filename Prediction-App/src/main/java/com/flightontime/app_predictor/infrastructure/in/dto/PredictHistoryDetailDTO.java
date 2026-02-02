package com.flightontime.app_predictor.infrastructure.in.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import java.util.List;

public record PredictHistoryDetailDTO(
        @JsonProperty("requestId")
        Long flightRequestId,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonProperty("flightDate")
        OffsetDateTime flightDateUtc,
        @JsonProperty("carrier")
        String airlineCode,
        @JsonProperty("origin")
        String originIata,
        @JsonProperty("destination")
        String destIata,
        String flightNumber,
        long uniqueUsersCount,
        List<PredictHistoryPredictionDTO> predictions
) {
}
