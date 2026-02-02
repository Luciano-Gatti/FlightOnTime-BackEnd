package com.flightontime.app_predictor.application.services;

import com.flightontime.app_predictor.domain.model.FlightRequest;
import com.flightontime.app_predictor.domain.model.Prediction;
import com.flightontime.app_predictor.domain.ports.in.PredictHistoryUseCase;
import com.flightontime.app_predictor.domain.ports.out.FlightRequestRepositoryPort;
import com.flightontime.app_predictor.domain.ports.out.PredictionRepositoryPort;
import com.flightontime.app_predictor.domain.ports.out.UserPredictionRepositoryPort;
import com.flightontime.app_predictor.infrastructure.in.dto.PredictHistoryDetailDTO;
import com.flightontime.app_predictor.infrastructure.in.dto.PredictHistoryItemDTO;
import com.flightontime.app_predictor.infrastructure.in.dto.PredictHistoryPredictionDTO;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class PredictHistoryService implements PredictHistoryUseCase {
    private final FlightRequestRepositoryPort flightRequestRepositoryPort;
    private final PredictionRepositoryPort predictionRepositoryPort;
    private final UserPredictionRepositoryPort userPredictionRepositoryPort;

    public PredictHistoryService(
            FlightRequestRepositoryPort flightRequestRepositoryPort,
            PredictionRepositoryPort predictionRepositoryPort,
            UserPredictionRepositoryPort userPredictionRepositoryPort
    ) {
        this.flightRequestRepositoryPort = flightRequestRepositoryPort;
        this.predictionRepositoryPort = predictionRepositoryPort;
        this.userPredictionRepositoryPort = userPredictionRepositoryPort;
    }

    @Override
    public List<PredictHistoryItemDTO> getHistory(Long userId) {
        List<FlightRequest> requests = flightRequestRepositoryPort.findByUserId(userId);
        return requests.stream()
                .map(request -> toHistoryItem(userId, request))
                .collect(Collectors.toList());
    }

    @Override
    public PredictHistoryDetailDTO getHistoryDetail(Long userId, Long requestId) {
        FlightRequest request = flightRequestRepositoryPort.findById(requestId)
                .filter(found -> userId.equals(found.userId()))
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));
        long uniqueUsersCount = userPredictionRepositoryPort.countDistinctUsersByRequestId(requestId);
        List<PredictHistoryPredictionDTO> predictions = predictionRepositoryPort
                .findByRequestIdAndUserId(requestId, userId)
                .stream()
                .sorted(Comparator.comparing(Prediction::predictedAt).reversed())
                .map(this::toPredictionDto)
                .collect(Collectors.toList());
        return new PredictHistoryDetailDTO(
                request.id(),
                request.flightDateUtc(),
                request.airlineCode(),
                request.originIata(),
                request.destIata(),
                request.flightNumber(),
                uniqueUsersCount,
                predictions
        );
    }

    private PredictHistoryItemDTO toHistoryItem(Long userId, FlightRequest request) {
        List<Prediction> predictions = predictionRepositoryPort
                .findByRequestIdAndUserId(request.id(), userId);
        Optional<Prediction> latest = predictions.stream()
                .max(Comparator.comparing(Prediction::predictedAt));
        long uniqueUsersCount = userPredictionRepositoryPort.countDistinctUsersByRequestId(request.id());
        return new PredictHistoryItemDTO(
                request.id(),
                request.flightDateUtc(),
                request.airlineCode(),
                request.originIata(),
                request.destIata(),
                request.flightNumber(),
                latest.map(Prediction::predictedStatus).orElse(null),
                latest.map(Prediction::predictedProbability).orElse(null),
                latest.map(Prediction::confidence).orElse(null),
                latest.map(Prediction::thresholdUsed).orElse(null),
                latest.map(Prediction::modelVersion).orElse(null),
                latest.map(Prediction::predictedAt).orElse(null),
                uniqueUsersCount
        );
    }

    private PredictHistoryPredictionDTO toPredictionDto(Prediction prediction) {
        return new PredictHistoryPredictionDTO(
                prediction.predictedStatus(),
                prediction.predictedProbability(),
                prediction.confidence(),
                prediction.thresholdUsed(),
                prediction.modelVersion(),
                prediction.predictedAt()
        );
    }
}
