package com.flightspredictor.flights.domain.service.history;

import com.flightspredictor.flights.domain.dto.history.HistoryItemResponse;
import com.flightspredictor.flights.domain.entities.UserPredictionSnapshot;
import com.flightspredictor.flights.domain.enums.PredictedStatus;
import com.flightspredictor.flights.domain.repository.UserPredictionSnapshotRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HistoryService {

    private final UserPredictionSnapshotRepository snapshotRepository;

    public Page<HistoryItemResponse> getHistory(
            Long userId,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            String origin,
            String dest,
            PredictedStatus status,
            Pageable pageable
    ) {
        Page<UserPredictionSnapshot> page = snapshotRepository.findHistory(
                userId,
                fromDate,
                toDate,
                origin,
                dest,
                status,
                pageable
        );
        return page.map(snapshot -> new HistoryItemResponse(
                snapshot.getId(),
                snapshot.getFlightPrediction().getId(),
                snapshot.getFlightRequest().getFlightDateUtc(),
                snapshot.getFlightRequest().getOriginIata(),
                snapshot.getFlightRequest().getDestIata(),
                snapshot.getFlightPrediction().getPredictedStatus(),
                snapshot.getFlightPrediction().getPredictedProbability(),
                snapshot.getFlightPrediction().getConfidence(),
                snapshot.getCreatedAt()
        ));
    }
}
