package com.flightontime.app_predictor.infrastructure.out.adapters;

import com.flightontime.app_predictor.domain.model.FlightRequestPopularity;
import com.flightontime.app_predictor.domain.model.UserPrediction;
import com.flightontime.app_predictor.domain.ports.out.UserPredictionRepositoryPort;
import com.flightontime.app_predictor.infrastructure.out.entities.UserPredictionEntity;
import com.flightontime.app_predictor.infrastructure.out.repository.UserPredictionJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
public class UserPredictionJpaAdapter implements UserPredictionRepositoryPort {
    private final UserPredictionJpaRepository userPredictionJpaRepository;
    private final UserPredictionMapper userPredictionMapper = new UserPredictionMapper();

    public UserPredictionJpaAdapter(UserPredictionJpaRepository userPredictionJpaRepository) {
        this.userPredictionJpaRepository = userPredictionJpaRepository;
    }

    @Override
    public UserPrediction save(UserPrediction userPrediction) {
        if (userPrediction == null) {
            throw new IllegalArgumentException("User prediction is required");
        }
        UserPredictionEntity entity = resolveEntity(userPrediction.id());
        userPredictionMapper.toEntity(userPrediction, entity);
        return userPredictionMapper.toDomain(userPredictionJpaRepository.save(entity));
    }

    @Override
    public Optional<UserPrediction> findById(Long id) {
        return userPredictionJpaRepository.findById(id)
                .map(userPredictionMapper::toDomain);
    }

    @Override
    public long countDistinctUsersByRequestId(Long requestId) {
        return userPredictionJpaRepository.countDistinctUsersByRequestId(requestId);
    }

    @Override
    public List<Long> findDistinctUserIdsByRequestId(Long requestId) {
        return userPredictionJpaRepository.findDistinctUserIdsByRequestId(requestId);
    }

    @Override
    public Optional<UserPrediction> findLatestByUserIdAndRequestId(Long userId, Long requestId) {
        return userPredictionJpaRepository.findTopByUserIdAndRequestIdOrderByCreatedAtDesc(userId, requestId)
                .map(userPredictionMapper::toDomain);
    }

    private UserPredictionEntity resolveEntity(Long id) {
        if (id == null) {
            return new UserPredictionEntity();
        }
        return userPredictionJpaRepository.findById(id).orElseGet(UserPredictionEntity::new);
    }
}
