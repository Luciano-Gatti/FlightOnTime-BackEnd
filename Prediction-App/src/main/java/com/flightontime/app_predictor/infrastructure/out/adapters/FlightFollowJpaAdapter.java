package com.flightontime.app_predictor.infrastructure.out.adapters;

import com.flightontime.app_predictor.domain.model.FlightFollow;
import com.flightontime.app_predictor.domain.model.RefreshMode;
import com.flightontime.app_predictor.domain.ports.out.FlightFollowRepositoryPort;
import com.flightontime.app_predictor.infrastructure.out.entities.FlightFollowEntity;
import com.flightontime.app_predictor.infrastructure.out.repository.FlightFollowJpaRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class FlightFollowJpaAdapter implements FlightFollowRepositoryPort {
    private final FlightFollowJpaRepository flightFollowJpaRepository;
    private final FlightFollowMapper flightFollowMapper = new FlightFollowMapper();

    public FlightFollowJpaAdapter(FlightFollowJpaRepository flightFollowJpaRepository) {
        this.flightFollowJpaRepository = flightFollowJpaRepository;
    }

    @Override
    public FlightFollow save(FlightFollow flightFollow) {
        if (flightFollow == null) {
            throw new IllegalArgumentException("Flight follow is required");
        }
        FlightFollowEntity entity = resolveEntity(flightFollow.id());
        flightFollowMapper.toEntity(flightFollow, entity);
        return flightFollowMapper.toDomain(flightFollowJpaRepository.save(entity));
    }

    @Override
    public Optional<FlightFollow> findByUserIdAndRequestId(Long userId, Long requestId) {
        return flightFollowJpaRepository.findFirstByUserIdAndRequestId(userId, requestId)
                .map(flightFollowMapper::toDomain);
    }

    @Override
    public List<FlightFollow> findByRefreshModeAndFlightDateBetween(
            RefreshMode refreshMode,
            OffsetDateTime start,
            OffsetDateTime end
    ) {
        return flightFollowJpaRepository.findByRefreshModeAndFlightDateBetween(refreshMode, start, end)
                .stream()
                .map(flightFollowMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<FlightFollow> findByFlightDateBetween(OffsetDateTime start, OffsetDateTime end) {
        return flightFollowJpaRepository.findByFlightDateBetween(start, end)
                .stream()
                .map(flightFollowMapper::toDomain)
                .collect(Collectors.toList());
    }

    private FlightFollowEntity resolveEntity(Long id) {
        if (id == null) {
            return new FlightFollowEntity();
        }
        return flightFollowJpaRepository.findById(id).orElseGet(FlightFollowEntity::new);
    }
}
