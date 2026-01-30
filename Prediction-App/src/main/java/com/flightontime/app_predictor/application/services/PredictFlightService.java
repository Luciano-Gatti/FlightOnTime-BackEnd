package com.flightontime.app_predictor.application.services;

import com.flightontime.app_predictor.domain.model.PredictFlightCommand;
import com.flightontime.app_predictor.domain.ports.in.PredictFlightUseCase;
import com.flightontime.app_predictor.domain.ports.out.ModelPredictionPort;
import com.flightontime.app_predictor.infrastructure.in.dto.PredictRequestDTO;
import com.flightontime.app_predictor.infrastructure.in.dto.PredictResponseDTO;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.springframework.stereotype.Service;

@Service
public class PredictFlightService implements PredictFlightUseCase {
    private final ModelPredictionPort modelPredictionPort;

    public PredictFlightService(ModelPredictionPort modelPredictionPort) {
        this.modelPredictionPort = modelPredictionPort;
    }

    @Override
    public PredictResponseDTO predict(PredictRequestDTO request) {
        validateRequest(request);
        PredictFlightCommand command = new PredictFlightCommand(
                request.flDate(),
                request.carrier(),
                request.origin(),
                request.dest(),
                request.flightNumber()
        );
        var prediction = modelPredictionPort.requestPrediction(command);
        return new PredictResponseDTO(
                prediction.status(),
                prediction.probability(),
                prediction.modelVersion(),
                OffsetDateTime.now(ZoneOffset.UTC)
        );
    }

    private void validateRequest(PredictRequestDTO request) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        if (request.flDate() == null || !request.flDate().isAfter(now)) {
            throw new IllegalArgumentException("flDate must be in the future");
        }
        if (request.origin() == null || request.origin().length() != 3) {
            throw new IllegalArgumentException("origin must be length 3");
        }
        if (request.dest() == null || request.dest().length() != 3) {
            throw new IllegalArgumentException("dest must be length 3");
        }
    }
}
