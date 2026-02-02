package com.flightontime.app_predictor.application.services;

import com.flightontime.app_predictor.domain.model.FlightRequest;
import com.flightontime.app_predictor.domain.model.FlightFollow;
import com.flightontime.app_predictor.domain.model.RefreshMode;
import com.flightontime.app_predictor.domain.ports.in.PredictFlightUseCase;
import com.flightontime.app_predictor.domain.ports.out.FlightFollowRepositoryPort;
import com.flightontime.app_predictor.domain.ports.out.FlightRequestRepositoryPort;
import com.flightontime.app_predictor.infrastructure.in.dto.PredictRequestDTO;
import com.flightontime.app_predictor.infrastructure.in.dto.PredictResponseDTO;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PredictFlightService implements PredictFlightUseCase {
    private static final Logger log = LoggerFactory.getLogger(PredictFlightService.class);
    private final FlightRequestRepositoryPort flightRequestRepositoryPort;
    private final FlightFollowRepositoryPort flightFollowRepositoryPort;
    private final PredictionWorkflowService predictionWorkflowService;

    public PredictFlightService(
            FlightRequestRepositoryPort flightRequestRepositoryPort,
            FlightFollowRepositoryPort flightFollowRepositoryPort,
            PredictionWorkflowService predictionWorkflowService
    ) {
        this.flightRequestRepositoryPort = flightRequestRepositoryPort;
        this.flightFollowRepositoryPort = flightFollowRepositoryPort;
        this.predictionWorkflowService = predictionWorkflowService;
    }

    @Override
    public PredictResponseDTO predict(PredictRequestDTO request, Long userId) {
        validateRequest(request);
        log.info("Starting prediction workflow userId={} request={}", userId, request);
        var workflowResult = predictionWorkflowService.predict(
                request.flightDateUtc(),
                request.airlineCode(),
                request.originIata(),
                request.destIata(),
                request.flightNumber(),
                userId,
                false,
                true
        );
        log.info("Prediction workflow completed userId={} predictionId={} requestId={}",
                userId,
                workflowResult.prediction() != null ? workflowResult.prediction().id() : null,
                workflowResult.flightRequest() != null ? workflowResult.flightRequest().id() : null);
        if (userId != null && workflowResult.prediction() != null && workflowResult.flightRequest() != null) {
            upsertFlightFollow(
                    userId,
                    workflowResult.flightRequest().id(),
                    workflowResult.prediction().id(),
                    RefreshMode.T72_REFRESH
            );
        }
        return new PredictResponseDTO(
                workflowResult.result().predictedStatus(),
                workflowResult.result().predictedProbability(),
                workflowResult.result().confidence(),
                workflowResult.result().thresholdUsed(),
                workflowResult.result().modelVersion(),
                workflowResult.result().predictedAt()
        );
    }

    @Override
    public PredictResponseDTO getLatestPrediction(Long requestId, Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User is required");
        }
        log.info("Fetching latest prediction userId={} requestId={}", userId, requestId);
        FlightRequest flightRequest = flightRequestRepositoryPort.findById(requestId)
                .filter(request -> userId.equals(request.userId()))
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));
        var result = predictionWorkflowService.getOrCreatePredictionForRequest(flightRequest);
        if (userId != null && result.prediction() != null) {
            upsertFlightFollow(
                    userId,
                    flightRequest.id(),
                    result.prediction().id(),
                    RefreshMode.T72_REFRESH
            );
        }
        log.info("Latest prediction resolved userId={} requestId={} predictionId={}",
                userId, requestId, result.prediction() != null ? result.prediction().id() : null);
        return new PredictResponseDTO(
                result.result().predictedStatus(),
                result.result().predictedProbability(),
                result.result().confidence(),
                result.result().thresholdUsed(),
                result.result().modelVersion(),
                result.result().predictedAt()
        );
    }

    private void validateRequest(PredictRequestDTO request) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        if (request.flightDateUtc() == null || !request.flightDateUtc().isAfter(now)) {
            throw new IllegalArgumentException("flightDateUtc must be in the future");
        }
        if (request.originIata() == null || request.originIata().length() != 3) {
            throw new IllegalArgumentException("originIata must be length 3");
        }
        if (request.destIata() == null || request.destIata().length() != 3) {
            throw new IllegalArgumentException("destIata must be length 3");
        }
        if (request.airlineCode() == null || request.airlineCode().isBlank()) {
            throw new IllegalArgumentException("airlineCode is required");
        }
    }

    private void upsertFlightFollow(
            Long userId,
            Long flightRequestId,
            Long baselineFlightPredictionId,
            RefreshMode refreshMode
    ) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        FlightFollow flightFollow = flightFollowRepositoryPort
                .findByUserIdAndFlightRequestId(userId, flightRequestId)
                .map(existing -> new FlightFollow(
                        existing.id(),
                        userId,
                        flightRequestId,
                        refreshMode,
                        baselineFlightPredictionId,
                        existing.createdAt(),
                        now
                ))
                .orElseGet(() -> new FlightFollow(
                        null,
                        userId,
                        flightRequestId,
                        refreshMode,
                        baselineFlightPredictionId,
                        now,
                        now
                ));
        flightFollowRepositoryPort.save(flightFollow);
    }

}
