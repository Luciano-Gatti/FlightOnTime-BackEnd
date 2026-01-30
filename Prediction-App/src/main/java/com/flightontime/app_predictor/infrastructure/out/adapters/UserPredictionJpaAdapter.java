package com.flightontime.app_predictor.infrastructure.out.adapters;

import com.flightontime.app_predictor.domain.model.FlightRequestPopularity;
import com.flightontime.app_predictor.domain.model.UserPrediction;
import com.flightontime.app_predictor.domain.ports.out.UserPredictionRepositoryPort;
import com.flightontime.app_predictor.infrastructure.out.entities.UserPredictionSnapshotEntity;
import com.flightontime.app_predictor.infrastructure.out.repository.UserPredictionSnapshotJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
public class UserPredictionJpaAdapter implements UserPredictionRepositoryPort {
    private final UserPredictionSnapshotJpaRepository userPredictionSnapshotJpaRepository;
    private final UserPredictionMapper userPredictionMapper = new UserPredictionMapper();

    public UserPredictionJpaAdapter(UserPredictionSnapshotJpaRepository userPredictionSnapshotJpaRepository) {
        this.userPredictionSnapshotJpaRepository = userPredictionSnapshotJpaRepository;
    }

    @Override
    public UserPrediction save(UserPrediction userPrediction) {
        if (userPrediction == null) {
            throw new IllegalArgumentException("User prediction is required");
        }
        UserPredictionSnapshotEntity entity = resolveEntity(userPrediction.id());
        userPredictionMapper.toEntity(userPrediction, entity);
        return userPredictionMapper.toDomain(userPredictionSnapshotJpaRepository.save(entity));
    }

    @Override
    public Optional<UserPrediction> findById(Long id) {
        return userPredictionSnapshotJpaRepository.findById(id)
                .map(userPredictionMapper::toDomain);
    }

    @Override
    public long countDistinctUsersByRequestId(Long requestId) {
        return userPredictionSnapshotJpaRepository.countDistinctUsersByRequestId(requestId);
    }

    @Override
    public List<Long> findDistinctUserIdsByRequestId(Long requestId) {
        return userPredictionSnapshotJpaRepository.findDistinctUserIdsByRequestId(requestId);
    }

    @Override
    public Optional<UserPrediction> findLatestByUserIdAndRequestId(Long userId, Long requestId) {
        return userPredictionSnapshotJpaRepository.findTopByUserIdAndRequestIdOrderByCreatedAtDesc(userId, requestId)
                .map(userPredictionMapper::toDomain);
    }

    private UserPredictionSnapshotEntity resolveEntity(Long id) {
        if (id == null) {
            return new UserPredictionSnapshotEntity();
        }
        return userPredictionSnapshotJpaRepository.findById(id).orElseGet(UserPredictionSnapshotEntity::new);
    }
}
