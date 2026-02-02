package com.flightontime.app_predictor.infrastructure.out.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.time.OffsetDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AeroDataBoxFlightActualResponse(
        String status,
        String statusCode,
        @JsonDeserialize(using = FlexibleOffsetDateTimeDeserializer.class)
        OffsetDateTime actualDeparture,
        @JsonDeserialize(using = FlexibleOffsetDateTimeDeserializer.class)
        OffsetDateTime actualArrival,
        @JsonDeserialize(using = FlexibleOffsetDateTimeDeserializer.class)
        OffsetDateTime departure,
        @JsonDeserialize(using = FlexibleOffsetDateTimeDeserializer.class)
        OffsetDateTime arrival
) {
}
