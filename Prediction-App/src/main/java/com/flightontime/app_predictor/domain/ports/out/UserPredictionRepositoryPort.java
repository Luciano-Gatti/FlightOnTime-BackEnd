package com.flightontime.app_predictor.domain.ports.out;

import com.flightontime.app_predictor.domain.model.UserPrediction;
import com.flightontime.app_predictor.domain.model.FlightRequestPopularity;
import java.util.List;
import java.util.Optional;

public interface UserPredictionRepositoryPort {
    UserPrediction save(UserPrediction userPrediction);

    Optional<UserPrediction> findById(Long id);

    long countDistinctUsersByRequestId(Long requestId);

    List<Long> findDistinctUserIdsByRequestId(Long requestId);

    Optional<UserPrediction> findLatestByUserIdAndRequestId(Long userId, Long requestId);

    List<FlightRequestPopularity> findTopRequestPopularity(int limit);
}
