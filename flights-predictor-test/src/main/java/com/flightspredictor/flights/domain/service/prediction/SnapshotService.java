package com.flightspredictor.flights.domain.service.prediction;

import com.flightspredictor.flights.domain.entities.FlightPrediction;
import com.flightspredictor.flights.domain.entities.FlightRequest;
import com.flightspredictor.flights.domain.entities.User;
import com.flightspredictor.flights.domain.entities.UserPredictionSnapshot;
import com.flightspredictor.flights.domain.enums.SnapshotSource;
import com.flightspredictor.flights.domain.repository.UserPredictionSnapshotRepository;
import com.flightspredictor.flights.domain.repository.UserRepository;
import com.flightspredictor.flights.infra.security.UserPrincipal;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SnapshotService {

    private final UserRepository userRepository;
    private final UserPredictionSnapshotRepository snapshotRepository;

    public void recordSnapshotIfAuthenticated(FlightRequest request, FlightPrediction prediction) {
        Long userId = resolveUserId();
        String correlationId = MDC.get("correlationId");
        if (userId == null) {
            log.info("SNAPSHOT_SKIPPED_ANONYMOUS correlationId={}", correlationId);
            return;
        }

        if (snapshotRepository.existsByUserIdAndFlightPredictionId(userId, prediction.getId())) {
            return;
        }

        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            return;
        }

        UserPredictionSnapshot snapshot = new UserPredictionSnapshot(
                null,
                user.get(),
                request,
                prediction,
                null,
                SnapshotSource.USER_QUERY
        );
        snapshotRepository.save(snapshot);
        log.info(
                "SNAPSHOT_CREATED correlationId={} userId={} predictionId={}",
                correlationId,
                userId,
                prediction.getId()
        );
    }

    private Long resolveUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserPrincipal userPrincipal) {
            return userPrincipal.getId();
        }

        Object details = authentication.getDetails();
        if (details instanceof java.util.Map<?, ?> map) {
            Object userId = map.get("userId");
            if (userId instanceof Long id) {
                return id;
            }
            if (userId instanceof Integer id) {
                return id.longValue();
            }
            if (userId instanceof String id) {
                try {
                    return Long.parseLong(id);
                } catch (NumberFormatException ignored) {
                    return null;
                }
            }
        }

        return null;
    }
}
