package com.flightontime.app_predictor.application.services;

import com.flightontime.app_predictor.domain.model.FlightRequest;
import com.flightontime.app_predictor.domain.model.FlightFollow;
import com.flightontime.app_predictor.domain.model.NotificationLog;
import com.flightontime.app_predictor.domain.model.PredictFlightCommand;
import com.flightontime.app_predictor.domain.model.Prediction;
import com.flightontime.app_predictor.domain.model.UserPrediction;
import com.flightontime.app_predictor.domain.ports.in.DistanceUseCase;
import com.flightontime.app_predictor.domain.ports.out.FlightFollowRepositoryPort;
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
    private final FlightFollowRepositoryPort flightFollowRepositoryPort;
    private final UserPredictionRepositoryPort userPredictionRepositoryPort;
    private final PredictionRepositoryPort predictionRepositoryPort;
    private final ModelPredictionPort modelPredictionPort;
    private final NotificationPort notificationPort;
    private final NotificationLogRepositoryPort notificationLogRepositoryPort;
    private final DistanceUseCase distanceUseCase;

    public T12hNotifyJobService(
            FlightRequestRepositoryPort flightRequestRepositoryPort,
            FlightFollowRepositoryPort flightFollowRepositoryPort,
            UserPredictionRepositoryPort userPredictionRepositoryPort,
            PredictionRepositoryPort predictionRepositoryPort,
            ModelPredictionPort modelPredictionPort,
            NotificationPort notificationPort,
            NotificationLogRepositoryPort notificationLogRepositoryPort,
            DistanceUseCase distanceUseCase
    ) {
        this.flightRequestRepositoryPort = flightRequestRepositoryPort;
        this.flightFollowRepositoryPort = flightFollowRepositoryPort;
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
        List<FlightFollow> follows = flightFollowRepositoryPort.findByFlightDateBetween(windowStart, windowEnd);
        if (follows.isEmpty()) {
            return;
        }
        List<Long> requestIds = follows.stream()
                .map(FlightFollow::requestId)
                .distinct()
                .toList();
        List<FlightRequest> requests = flightRequestRepositoryPort.findByIds(requestIds);
        var requestLookup = requests.stream()
                .collect(java.util.stream.Collectors.toMap(FlightRequest::id, item -> item));
        for (FlightFollow follow : follows) {
            FlightRequest request = requestLookup.get(follow.requestId());
            if (request == null) {
                continue;
            }
            processUserNotification(follow.userId(), request, follow, now);
        }
        dispatchNotifications(notificationsByUser, now);
    }

    private void processUserNotification(
            Long userId,
            FlightRequest request,
            FlightFollow follow,
            OffsetDateTime now
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
        Long baselinePredictionId = follow.baselinePredictionId();
        if (baselinePredictionId == null) {
            baselinePredictionId = baselineUserPrediction.get().predictionId();
        }
        Optional<Prediction> baselinePrediction = predictionRepositoryPort.findById(baselinePredictionId);
        if (baselinePrediction.isEmpty()) {
            return;
        }
        Prediction currentPrediction = getOrCreateCurrentPrediction(request, now);
        if (!isRelevantStatusChange(baselinePrediction.get().status(), currentPrediction.status())) {
            return;
        }
        NotificationCandidate candidate = new NotificationCandidate(
                userId,
                request,
                baselineUserPrediction.get().id(),
                currentPrediction.status(),
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
                        candidate.status(),
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
        return "Flight " + flightLabel + " cambió de " + baseline.status() + " a " + current.status();
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
                modelPrediction.status(),
                modelPrediction.probability(),
                modelPrediction.modelVersion(),
                now,
                now
        );
        return predictionRepositoryPort.save(prediction);
    }

    private PredictFlightCommand buildCommand(FlightRequest request) {
        double distance = distanceUseCase.calculateDistance(request.origin(), request.destination());
        return new PredictFlightCommand(
                toUtc(request.flightDate()),
                request.carrier(),
                request.origin(),
                request.destination(),
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
            String status,
            String message
    ) {
    }
}
