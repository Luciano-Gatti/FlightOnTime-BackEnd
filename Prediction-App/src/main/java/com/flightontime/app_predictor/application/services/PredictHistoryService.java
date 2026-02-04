package com.flightontime.app_predictor.application.services;

import com.flightontime.app_predictor.domain.model.FlightRequest;
import com.flightontime.app_predictor.domain.model.PredictHistoryDetail;
import com.flightontime.app_predictor.domain.model.PredictHistoryItem;
import com.flightontime.app_predictor.domain.model.PredictHistoryPrediction;
import com.flightontime.app_predictor.domain.model.Prediction;
import com.flightontime.app_predictor.domain.ports.in.PredictHistoryUseCase;
import com.flightontime.app_predictor.domain.ports.out.FlightRequestRepositoryPort;
import com.flightontime.app_predictor.domain.ports.out.PredictionRepositoryPort;
import com.flightontime.app_predictor.domain.ports.out.UserPredictionRepositoryPort;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * Clase PredictHistoryService.
 */
@Service
public class PredictHistoryService implements PredictHistoryUseCase {
    private final FlightRequestRepositoryPort flightRequestRepositoryPort;
    private final PredictionRepositoryPort predictionRepositoryPort;
    private final UserPredictionRepositoryPort userPredictionRepositoryPort;

    /**
     * Ejecuta la operación predict history service.
     * @param flightRequestRepositoryPort variable de entrada flightRequestRepositoryPort.
     * @param predictionRepositoryPort variable de entrada predictionRepositoryPort.
     * @param userPredictionRepositoryPort variable de entrada userPredictionRepositoryPort.
     */

    /**
     * Ejecuta la operación predict history service.
     * @param flightRequestRepositoryPort variable de entrada flightRequestRepositoryPort.
     * @param predictionRepositoryPort variable de entrada predictionRepositoryPort.
     * @param userPredictionRepositoryPort variable de entrada userPredictionRepositoryPort.
     * @return resultado de la operación predict history service.
     */

    public PredictHistoryService(
            FlightRequestRepositoryPort flightRequestRepositoryPort,
            PredictionRepositoryPort predictionRepositoryPort,
            UserPredictionRepositoryPort userPredictionRepositoryPort
    ) {
        this.flightRequestRepositoryPort = flightRequestRepositoryPort;
        this.predictionRepositoryPort = predictionRepositoryPort;
        this.userPredictionRepositoryPort = userPredictionRepositoryPort;
    }

    /**
     * Ejecuta la operación get history.
     * @param userId variable de entrada userId.
     * @return resultado de la operación get history.
     */
    @Override
    public List<PredictHistoryItem> getHistory(Long userId) {
        List<FlightRequest> requests = flightRequestRepositoryPort.findByUserId(userId);
        return requests.stream()
                .map(request -> toHistoryItem(userId, request))
                .collect(Collectors.toList());
    }

    /**
     * Ejecuta la operación get history detail.
     * @param userId variable de entrada userId.
     * @param requestId variable de entrada requestId.
     * @return resultado de la operación get history detail.
     */
    @Override
    public PredictHistoryDetail getHistoryDetail(Long userId, Long requestId) {
        FlightRequest request = flightRequestRepositoryPort.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));
        userPredictionRepositoryPort.findLatestByUserIdAndRequestId(userId, requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));
        long uniqueUsersCount = userPredictionRepositoryPort.countDistinctUsersByRequestId(requestId);
        List<PredictHistoryPrediction> predictions = predictionRepositoryPort
                .findByRequestIdAndUserId(requestId, userId)
                .stream()
                .sorted(Comparator.comparing(Prediction::predictedAt).reversed())
                .map(this::toPredictionDto)
                .collect(Collectors.toList());
        return new PredictHistoryDetail(
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

    /**
     * Ejecuta la operación to history item.
     * @param userId variable de entrada userId.
     * @param request variable de entrada request.
     * @return resultado de la operación to history item.
     */

    private PredictHistoryItem toHistoryItem(Long userId, FlightRequest request) {
        List<Prediction> predictions = predictionRepositoryPort
                .findByRequestIdAndUserId(request.id(), userId);
        Optional<Prediction> latest = predictions.stream()
                .max(Comparator.comparing(Prediction::predictedAt));
        long uniqueUsersCount = userPredictionRepositoryPort.countDistinctUsersByRequestId(request.id());
        return new PredictHistoryItem(
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

    /**
     * Ejecuta la operación to prediction dto.
     * @param prediction variable de entrada prediction.
     * @return resultado de la operación to prediction dto.
     */

    private PredictHistoryPrediction toPredictionDto(Prediction prediction) {
        return new PredictHistoryPrediction(
                prediction.predictedStatus(),
                prediction.predictedProbability(),
                prediction.confidence(),
                prediction.thresholdUsed(),
                prediction.modelVersion(),
                prediction.predictedAt()
        );
    }
}
