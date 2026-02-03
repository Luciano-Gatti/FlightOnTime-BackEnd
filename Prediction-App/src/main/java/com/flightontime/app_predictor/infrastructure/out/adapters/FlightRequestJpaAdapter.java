package com.flightontime.app_predictor.infrastructure.out.adapters;

import com.flightontime.app_predictor.domain.model.FlightRequest;
import com.flightontime.app_predictor.domain.ports.out.FlightRequestRepositoryPort;
import com.flightontime.app_predictor.infrastructure.out.entities.FlightRequestEntity;
import com.flightontime.app_predictor.infrastructure.out.repository.FlightRequestJpaRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class FlightRequestJpaAdapter implements FlightRequestRepositoryPort {
    private final FlightRequestJpaRepository flightRequestJpaRepository;
    private final FlightRequestMapper flightRequestMapper = new FlightRequestMapper();

    public FlightRequestJpaAdapter(FlightRequestJpaRepository flightRequestJpaRepository) {
        this.flightRequestJpaRepository = flightRequestJpaRepository;
    }

    @Override
    public FlightRequest save(FlightRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Flight request is required");
        }
        FlightRequestEntity entity = resolveEntity(request.id());
        flightRequestMapper.toEntity(request, entity);
        return flightRequestMapper.toDomain(flightRequestJpaRepository.save(entity));
    }

    @Override
    public Optional<FlightRequest> findById(Long id) {
        return flightRequestJpaRepository.findById(id)
                .map(flightRequestMapper::toDomain);
    }

    @Override
    public Optional<FlightRequest> findByFlight(
            OffsetDateTime flightDateUtc,
            String airlineCode,
            String originIata,
            String destIata
    ) {
        return flightRequestJpaRepository
                .findFirstByFlightDateUtcAndAirlineCodeAndOriginIataAndDestIata(
                        flightDateUtc,
                        airlineCode,
                        originIata,
                        destIata
                )
                .map(flightRequestMapper::toDomain);
    }

    @Override
    public List<FlightRequest> findByUserId(Long userId) {
        return flightRequestJpaRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(flightRequestMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<FlightRequest> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return flightRequestJpaRepository.findAllById(ids)
                .stream()
                .map(flightRequestMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<FlightRequest> findByFlightDateBetweenWithUserPredictions(
            OffsetDateTime start,
            OffsetDateTime end
    ) {
        return flightRequestJpaRepository.findByFlightDateBetweenWithUserPredictions(start, end)
                .stream()
                .map(flightRequestMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<FlightRequest> findByFlightDateBetweenWithoutActuals(
            OffsetDateTime start,
            OffsetDateTime end
    ) {
        return flightRequestJpaRepository.findByFlightDateBetweenWithoutActuals(start, end)
                .stream()
                .map(flightRequestMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<FlightRequest> findByFlightDateBeforeAndActive(OffsetDateTime cutoff) {
        return flightRequestJpaRepository.findByFlightDateBeforeAndActive(cutoff)
                .stream()
                .map(flightRequestMapper::toDomain)
                .collect(Collectors.toList());
    }

    private FlightRequestEntity resolveEntity(Long id) {
        if (id == null) {
            return new FlightRequestEntity();
        }
        return flightRequestJpaRepository.findById(id).orElseGet(FlightRequestEntity::new);
    }
}
