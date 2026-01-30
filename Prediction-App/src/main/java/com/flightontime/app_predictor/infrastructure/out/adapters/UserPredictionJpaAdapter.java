package com.flightontime.app_predictor.infrastructure.out.adapters;

import com.flightontime.app_predictor.domain.model.UserPrediction;
import com.flightontime.app_predictor.domain.ports.out.UserPredictionRepositoryPort;
import com.flightontime.app_predictor.infrastructure.out.entities.UserPredictionEntity;
import com.flightontime.app_predictor.infrastructure.out.repository.UserPredictionJpaRepository;
import java.util.Optional;
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

    private UserPredictionEntity resolveEntity(Long id) {
        if (id == null) {
            return new UserPredictionEntity();
        }
        return userPredictionJpaRepository.findById(id).orElseGet(UserPredictionEntity::new);
    }
}
