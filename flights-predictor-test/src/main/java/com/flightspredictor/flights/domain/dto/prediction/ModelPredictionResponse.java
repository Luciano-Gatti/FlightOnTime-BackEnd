package com.flightspredictor.flights.domain.dto.prediction;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.flightspredictor.flights.domain.entities.FlightPrediction;
import com.flightspredictor.flights.domain.enums.PredictedStatus;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ModelPredictionResponse(

        @JsonProperty("prediction")
        PredictedStatus predictedStatus,

        @JsonProperty("probability")
        Double predictedProbability,

        @JsonProperty("threshold")
        String confidence
) {
    public ModelPredictionResponse(FlightPrediction response) {
        this (
                response.getPredictedStatus(),
                response.getPredictedProbability(),
                response.getConfidence()
        );
    }
}
