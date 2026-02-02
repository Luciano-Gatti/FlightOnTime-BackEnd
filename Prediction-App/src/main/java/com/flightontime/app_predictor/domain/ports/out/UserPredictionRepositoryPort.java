package com.flightontime.app_predictor.domain.ports.out;

import com.flightontime.app_predictor.domain.model.UserPrediction;
import java.util.List;
import java.util.Optional;

public interface UserPredictionRepositoryPort {
    UserPrediction save(UserPrediction userPrediction);

    Optional<UserPrediction> findById(Long id);

    long countDistinctUsersByRequestId(Long flightRequestId);

    List<Long> findDistinctUserIdsByRequestId(Long flightRequestId);

    Optional<UserPrediction> findLatestByUserIdAndRequestId(Long userId, Long flightRequestId);
}
