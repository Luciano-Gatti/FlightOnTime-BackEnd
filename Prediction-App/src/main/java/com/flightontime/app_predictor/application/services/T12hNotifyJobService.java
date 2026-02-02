package com.flightontime.app_predictor.application.services;

import com.flightontime.app_predictor.domain.model.FlightRequest;
import com.flightontime.app_predictor.domain.model.NotificationLog;
import com.flightontime.app_predictor.domain.model.PredictFlightCommand;
import com.flightontime.app_predictor.domain.model.Prediction;
import com.flightontime.app_predictor.domain.model.UserPrediction;
import com.flightontime.app_predictor.domain.ports.in.DistanceUseCase;
import com.flightontime.app_predictor.domain.ports.out.FlightRequestRepositoryPort;
import com.flightontime.app_predictor.domain.ports.out.ModelPredictionPort;
import com.flightontime.app_predictor.domain.ports.out.NotificationLogRepositoryPort;
import com.flightontime.app_predictor.domain.ports.out.NotificationPort;
import com.flightontime.app_predictor.domain.ports.out.PredictionRepositoryPort;
import com.flightontime.app_predictor.domain.ports.out.UserPredictionRepositoryPort;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class T12hNotifyJobService {
    private static final String NOTIFICATION_TYPE = "T12H";
    private static final String CHANNEL = "SYSTEM";

    private final FlightRequestRepositoryPort flightRequestRepositoryPort;
    private final UserPredictionRepositoryPort userPredictionRepositoryPort;
    private final PredictionRepositoryPort predictionRepositoryPort;
    private final ModelPredictionPort modelPredictionPort;
    private final NotificationPort notificationPort;
    private final NotificationLogRepositoryPort notificationLogRepositoryPort;
    private final DistanceUseCase distanceUseCase;

    public T12hNotifyJobService(
            FlightRequestRepositoryPort flightRequestRepositoryPort,
            UserPredictionRepositoryPort userPredictionRepositoryPort,
            PredictionRepositoryPort predictionRepositoryPort,
            ModelPredictionPort modelPredictionPort,
            NotificationPort notificationPort,
            NotificationLogRepositoryPort notificationLogRepositoryPort,
            DistanceUseCase distanceUseCase
    ) {
        this.flightRequestRepositoryPort = flightRequestRepositoryPort;
        this.userPredictionRepositoryPort = userPredictionRepositoryPort;
        this.predictionRepositoryPort = predictionRepositoryPort;
        this.modelPredictionPort = modelPredictionPort;
        this.notificationPort = notificationPort;
        this.notificationLogRepositoryPort = notificationLogRepositoryPort;
        this.distanceUseCase = distanceUseCase;
    }

    public void notifyUsers() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime windowStart = now.plusHours(11);
        OffsetDateTime windowEnd = now.plusHours(13);
        List<FlightRequest> requests = flightRequestRepositoryPort
                .findByFlightDateBetweenWithUserPredictions(windowStart, windowEnd);
        Map<Long, List<NotificationCandidate>> notificationsByUser = new HashMap<>();
        for (FlightRequest request : requests) {
            if (closeIfExpired(request, now)) {
                continue;
            }
            List<Long> userIds = userPredictionRepositoryPort.findDistinctUserIdsByRequestId(request.id());
            for (Long userId : userIds) {
                processUserNotification(userId, request, now, notificationsByUser);
            }
        }
        dispatchNotifications(notificationsByUser, now);
    }

    private void processUserNotification(
            Long userId,
            FlightRequest request,
            OffsetDateTime now,
            Map<Long, List<NotificationCandidate>> notificationsByUser
    ) {
        Optional<NotificationLog> existing = notificationLogRepositoryPort
                .findByUserIdAndRequestIdAndType(userId, request.id(), NOTIFICATION_TYPE);
        if (existing.isPresent()) {
            return;
        }
        Optional<UserPrediction> baselineUserPrediction = userPredictionRepositoryPort
                .findLatestByUserIdAndRequestId(userId, request.id());
        if (baselineUserPrediction.isEmpty()) {
            return;
        }
        Optional<Prediction> baselinePrediction = predictionRepositoryPort
                .findById(baselineUserPrediction.get().flightPredictionId());
        if (baselinePrediction.isEmpty()) {
            return;
        }
        Prediction currentPrediction = getOrCreateCurrentPrediction(request, now);
        if (!isRelevantStatusChange(
                baselinePrediction.get().predictedStatus(),
                currentPrediction.predictedStatus()
        )) {
            return;
        }
        NotificationCandidate candidate = new NotificationCandidate(
                userId,
                request,
                baselineUserPrediction.get().id(),
                currentPrediction.predictedStatus(),
                buildMessage(request, baselinePrediction.get(), currentPrediction)
        );
        notificationsByUser
                .computeIfAbsent(userId, ignored -> new ArrayList<>())
                .add(candidate);
    }

    private void dispatchNotifications(
            Map<Long, List<NotificationCandidate>> notificationsByUser,
            OffsetDateTime now
    ) {
        for (Map.Entry<Long, List<NotificationCandidate>> entry : notificationsByUser.entrySet()) {
            List<NotificationCandidate> candidates = entry.getValue();
            if (candidates == null || candidates.isEmpty()) {
                continue;
            }
            List<String> messages = candidates.stream()
                    .map(NotificationCandidate::message)
                    .toList();
            notificationPort.sendT12hSummary(entry.getKey(), messages);
            for (NotificationCandidate candidate : candidates) {
                NotificationLog log = new NotificationLog(
                        null,
                        candidate.userId(),
                        candidate.request().id(),
                        NOTIFICATION_TYPE,
                        candidate.userPredictionId(),
                        CHANNEL,
                        candidate.predictedStatus(),
                        candidate.message(),
                        now,
                        now
                );
                notificationLogRepositoryPort.save(log);
            }
        }
    }

    private String buildMessage(FlightRequest request, Prediction baseline, Prediction current) {
        String flightLabel = hasFlightNumber(request.flightNumber())
                ? request.flightNumber()
                : "Request " + request.id();
        return "Flight " + flightLabel + " cambió de " + baseline.predictedStatus()
                + " a " + current.predictedStatus();
    }

    private boolean isRelevantStatusChange(String baselineStatus, String currentStatus) {
        if (baselineStatus == null || currentStatus == null) {
            return false;
        }
        boolean baselineSupported = isOnTimeOrDelayed(baselineStatus);
        boolean currentSupported = isOnTimeOrDelayed(currentStatus);
        return baselineSupported && currentSupported && !baselineStatus.equals(currentStatus);
    }

    private boolean isOnTimeOrDelayed(String status) {
        return "ON_TIME".equals(status) || "DELAYED".equals(status);
    }

    private boolean hasFlightNumber(String flightNumber) {
        return flightNumber != null && !flightNumber.isBlank();
    }

    private boolean closeIfExpired(FlightRequest request, OffsetDateTime nowUtc) {
        if (request == null || request.flightDateUtc() == null || !request.active()) {
            return true;
        }
        if (request.flightDateUtc().isBefore(nowUtc)) {
            FlightRequest closedRequest = new FlightRequest(
                    request.id(),
                    request.userId(),
                    request.flightDateUtc(),
                    request.airlineCode(),
                    request.originIata(),
                    request.destIata(),
                    request.flightNumber(),
                    request.createdAt(),
                    false,
                    nowUtc
            );
            flightRequestRepositoryPort.save(closedRequest);
            return true;
        }
        return false;
    }

    private Prediction getOrCreateCurrentPrediction(FlightRequest request, OffsetDateTime now) {
        OffsetDateTime bucketStart = resolveBucketStart(now);
        OffsetDateTime bucketEnd = bucketStart.plusHours(3);
        Optional<Prediction> cached = predictionRepositoryPort.findByRequestIdAndPredictedAtBetween(
                request.id(),
                bucketStart,
                bucketEnd
        );
        if (cached.isPresent()) {
            return cached.get();
        }
        PredictFlightCommand command = buildCommand(request);
        var modelPrediction = modelPredictionPort.requestPrediction(command);
        Prediction prediction = new Prediction(
                null,
                request.id(),
                modelPrediction.predictedStatus(),
                modelPrediction.predictedProbability(),
                modelPrediction.confidence(),
                modelPrediction.thresholdUsed(),
                modelPrediction.modelVersion(),
                now,
                now
        );
        return predictionRepositoryPort.save(prediction);
    }

    private PredictFlightCommand buildCommand(FlightRequest request) {
        double distance = distanceUseCase.calculateDistance(request.originIata(), request.destIata());
        return new PredictFlightCommand(
                toUtc(request.flightDateUtc()),
                request.airlineCode(),
                request.originIata(),
                request.destIata(),
                request.flightNumber(),
                distance
        );
    }

    private OffsetDateTime resolveBucketStart(OffsetDateTime timestamp) {
        OffsetDateTime normalized = timestamp.withMinute(0).withSecond(0).withNano(0);
        int offset = normalized.getHour() % 3;
        return normalized.minusHours(offset);
    }

    private OffsetDateTime toUtc(OffsetDateTime value) {
        if (value == null) {
            return null;
        }
        return value.withOffsetSameInstant(ZoneOffset.UTC);
    }

    private record NotificationCandidate(
            Long userId,
            FlightRequest request,
            Long userPredictionId,
            String predictedStatus,
            String message
    ) {
    }
}
