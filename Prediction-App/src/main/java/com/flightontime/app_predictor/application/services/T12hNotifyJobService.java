package com.flightontime.app_predictor.application.services;

import com.flightontime.app_predictor.domain.model.FlightRequest;
import com.flightontime.app_predictor.domain.model.NotificationLog;
import com.flightontime.app_predictor.domain.model.PredictFlightCommand;
import com.flightontime.app_predictor.domain.model.Prediction;
import com.flightontime.app_predictor.domain.model.PredictionSource;
import com.flightontime.app_predictor.domain.model.RefreshMode;
import com.flightontime.app_predictor.domain.model.UserPrediction;
import com.flightontime.app_predictor.domain.ports.in.DistanceUseCase;
import com.flightontime.app_predictor.domain.ports.out.FlightFollowRepositoryPort;
import com.flightontime.app_predictor.domain.ports.out.FlightRequestRepositoryPort;
import com.flightontime.app_predictor.domain.ports.out.ModelPredictionPort;
import com.flightontime.app_predictor.domain.ports.out.NotificationLogRepositoryPort;
import com.flightontime.app_predictor.domain.ports.out.NotificationPort;
import com.flightontime.app_predictor.domain.ports.out.PredictionRepositoryPort;
import com.flightontime.app_predictor.domain.ports.out.UserPredictionRepositoryPort;
import java.time.Duration;
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
    private static final Duration EARLY_WINDOW_START = Duration.ofHours(11);
    private static final Duration WINDOW_END = Duration.ofHours(13);

    private final FlightFollowRepositoryPort flightFollowRepositoryPort;
    private final FlightRequestRepositoryPort flightRequestRepositoryPort;
    private final UserPredictionRepositoryPort userPredictionRepositoryPort;
    private final PredictionRepositoryPort predictionRepositoryPort;
    private final ModelPredictionPort modelPredictionPort;
    private final NotificationPort notificationPort;
    private final NotificationLogRepositoryPort notificationLogRepositoryPort;
    private final DistanceUseCase distanceUseCase;

    public T12hNotifyJobService(
            FlightFollowRepositoryPort flightFollowRepositoryPort,
            FlightRequestRepositoryPort flightRequestRepositoryPort,
            UserPredictionRepositoryPort userPredictionRepositoryPort,
            PredictionRepositoryPort predictionRepositoryPort,
            ModelPredictionPort modelPredictionPort,
            NotificationPort notificationPort,
            NotificationLogRepositoryPort notificationLogRepositoryPort,
            DistanceUseCase distanceUseCase
    ) {
        this.flightFollowRepositoryPort = flightFollowRepositoryPort;
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
        OffsetDateTime windowStart = now;
        OffsetDateTime windowEnd = now.plusHours(13);
        var follows = flightFollowRepositoryPort.findByFlightDateBetween(windowStart, windowEnd);
        Map<Long, List<NotificationCandidate>> notificationsByUser = new HashMap<>();
        for (var follow : follows) {
            if (!isRelevantRefreshMode(follow.refreshMode())) {
                continue;
            }
            FlightRequest request = flightRequestRepositoryPort.findById(follow.flightRequestId())
                    .orElse(null);
            if (request == null || !request.active() || request.flightDateUtc() == null) {
                continue;
            }
            Duration timeToDeparture = Duration.between(now, request.flightDateUtc());
            if (timeToDeparture.isNegative()) {
                continue;
            }
            if (timeToDeparture.compareTo(WINDOW_END) > 0) {
                continue;
            }
            boolean lateNotification = timeToDeparture.compareTo(EARLY_WINDOW_START) < 0;
            processUserNotification(
                    follow.userId(),
                    follow.baselineSnapshotId(),
                    request,
                    now,
                    lateNotification,
                    notificationsByUser
            );
        }
        dispatchNotifications(notificationsByUser, now);
    }

    private void processUserNotification(
            Long userId,
            Long baselineSnapshotId,
            FlightRequest request,
            OffsetDateTime now,
            boolean lateNotification,
            Map<Long, List<NotificationCandidate>> notificationsByUser
    ) {
        Optional<NotificationLog> existing = notificationLogRepositoryPort
                .findByUserIdAndRequestIdAndType(userId, request.id(), NOTIFICATION_TYPE);
        if (existing.isPresent()) {
            return;
        }
        Optional<UserPrediction> baselineUserPrediction = resolveBaselineSnapshot(userId, baselineSnapshotId, request.id());
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
                buildMessage(request, baselinePrediction.get(), currentPrediction, lateNotification)
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

    private String buildMessage(
            FlightRequest request,
            Prediction baseline,
            Prediction current,
            boolean lateNotification
    ) {
        String flightLabel = hasFlightNumber(request.flightNumber())
                ? request.flightNumber()
                : "Request " + request.id();
        String baseMessage = "Flight " + flightLabel + " cambió de " + baseline.predictedStatus()
                + " a " + current.predictedStatus();
        if (!lateNotification) {
            return baseMessage;
        }
        return baseMessage + " (notificación tardía)";
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
        Optional<Prediction> cached = predictionRepositoryPort.findByRequestIdAndForecastBucketUtc(
                request.id(),
                bucketStart
        );
        if (cached.isPresent()) {
            return cached.get();
        }
        PredictFlightCommand command = buildCommand(request);
        var modelPrediction = modelPredictionPort.requestPrediction(command);
        Prediction prediction = new Prediction(
                null,
                request.id(),
                bucketStart,
                modelPrediction.predictedStatus(),
                modelPrediction.predictedProbability(),
                modelPrediction.confidence(),
                modelPrediction.thresholdUsed(),
                modelPrediction.modelVersion(),
                PredictionSource.SYSTEM,
                now,
                now
        );
        return predictionRepositoryPort.save(prediction);
    }

    private PredictFlightCommand buildCommand(FlightRequest request) {
        double distance = request.distance() > 0 ? request.distance()
                : distanceUseCase.calculateDistance(request.originIata(), request.destIata());
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

    private Optional<UserPrediction> resolveBaselineSnapshot(
            Long userId,
            Long baselineSnapshotId,
            Long flightRequestId
    ) {
        if (baselineSnapshotId != null) {
            return userPredictionRepositoryPort.findById(baselineSnapshotId)
                    .filter(snapshot -> userId.equals(snapshot.userId())
                            && flightRequestId.equals(snapshot.flightRequestId()));
        }
        return userPredictionRepositoryPort.findLatestByUserIdAndRequestId(userId, flightRequestId);
    }

    private boolean isRelevantRefreshMode(RefreshMode refreshMode) {
        return RefreshMode.T72_REFRESH.equals(refreshMode) || RefreshMode.T12_ONLY.equals(refreshMode);
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
