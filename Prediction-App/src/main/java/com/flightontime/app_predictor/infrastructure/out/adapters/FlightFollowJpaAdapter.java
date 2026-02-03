package com.flightontime.app_predictor.infrastructure.out.adapters;

import com.flightontime.app_predictor.domain.model.FlightFollow;
import com.flightontime.app_predictor.domain.model.RefreshMode;
import com.flightontime.app_predictor.domain.ports.out.FlightFollowRepositoryPort;
import com.flightontime.app_predictor.infrastructure.out.entities.FlightSubscriptionEntity;
import com.flightontime.app_predictor.infrastructure.out.repository.FlightSubscriptionJpaRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Clase FlightFollowJpaAdapter.
 */
@Component
public class FlightFollowJpaAdapter implements FlightFollowRepositoryPort {
    private final FlightSubscriptionJpaRepository flightSubscriptionJpaRepository;
    private final FlightFollowMapper flightFollowMapper = new FlightFollowMapper();

    public FlightFollowJpaAdapter(FlightSubscriptionJpaRepository flightSubscriptionJpaRepository) {
        this.flightSubscriptionJpaRepository = flightSubscriptionJpaRepository;
    }

    @Override
    public FlightFollow save(FlightFollow flightFollow) {
        if (flightFollow == null) {
            throw new IllegalArgumentException("Flight follow is required");
        }
        FlightSubscriptionEntity entity = resolveEntity(flightFollow.id());
        flightFollowMapper.toEntity(flightFollow, entity);
        return flightFollowMapper.toDomain(flightSubscriptionJpaRepository.save(entity));
    }

    @Override
    public Optional<FlightFollow> findByUserIdAndFlightRequestId(Long userId, Long flightRequestId) {
        return flightSubscriptionJpaRepository.findFirstByUserIdAndFlightRequestId(userId, flightRequestId)
                .map(flightFollowMapper::toDomain);
    }

    @Override
    public List<FlightFollow> findByRefreshModeAndFlightDateBetween(
            RefreshMode refreshMode,
            OffsetDateTime start,
            OffsetDateTime end
    ) {
        return flightSubscriptionJpaRepository.findByRefreshModeAndFlightDateBetween(refreshMode, start, end)
                .stream()
                .map(flightFollowMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<FlightFollow> findByFlightDateBetween(OffsetDateTime start, OffsetDateTime end) {
        return flightSubscriptionJpaRepository.findByFlightDateBetween(start, end)
                .stream()
                .map(flightFollowMapper::toDomain)
                .collect(Collectors.toList());
    }

    private FlightSubscriptionEntity resolveEntity(Long id) {
        if (id == null) {
            return new FlightSubscriptionEntity();
        }
        return flightSubscriptionJpaRepository.findById(id).orElseGet(FlightSubscriptionEntity::new);
    }
}
