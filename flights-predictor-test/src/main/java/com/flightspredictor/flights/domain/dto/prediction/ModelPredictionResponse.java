package com.flightspredictor.flights.domain.dto.prediction;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.flightspredictor.flights.domain.enum.Prevision;
import com.flightspredictor.flights.domain.enum.Status;
import com.flightspredictor.flights.domain.entities.Prediction;
import com.flightspredictor.flights.domain.entities.Request;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ModelPredictionResponse(

        @JsonProperty("prediction")
        Prevision prevision,

        @JsonProperty("probability")
        Double probability,

        @JsonProperty("threshold")
        Status status
) {
    public ModelPredictionResponse(Prediction response) {
        this (
                response.getPrevision(),
                response.getProbability(),
                response.getStatus()
        );
    }
}
