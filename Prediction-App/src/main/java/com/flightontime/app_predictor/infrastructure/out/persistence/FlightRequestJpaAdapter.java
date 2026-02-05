package com.flightontime.app_predictor.infrastructure.out.persistence;

import com.flightontime.app_predictor.domain.model.FlightRequest;
import com.flightontime.app_predictor.domain.ports.out.FlightRequestRepositoryPort;
import com.flightontime.app_predictor.infrastructure.out.mapper.FlightRequestMapper;
import com.flightontime.app_predictor.infrastructure.out.persistence.entities.FlightRequestEntity;
import com.flightontime.app_predictor.infrastructure.out.persistence.repository.FlightRequestJpaRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Clase FlightRequestJpaAdapter.
 */
@Component
public class FlightRequestJpaAdapter implements FlightRequestRepositoryPort {
    private final FlightRequestJpaRepository flightRequestJpaRepository;
    private final FlightRequestMapper flightRequestMapper = new FlightRequestMapper();

    /**
     * Ejecuta la operación flight request jpa adapter.
     * @param flightRequestJpaRepository variable de entrada flightRequestJpaRepository.
     */

    /**
     * Ejecuta la operación flight request jpa adapter.
     * @param flightRequestJpaRepository variable de entrada flightRequestJpaRepository.
     * @return resultado de la operación flight request jpa adapter.
     */

    public FlightRequestJpaAdapter(FlightRequestJpaRepository flightRequestJpaRepository) {
        this.flightRequestJpaRepository = flightRequestJpaRepository;
    }

    /**
     * Ejecuta la operación save.
     * @param request variable de entrada request.
     * @return resultado de la operación save.
     */
    @Override
    public FlightRequest save(FlightRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Flight request is required");
        }
        FlightRequestEntity entity = resolveEntity(request.id());
        flightRequestMapper.toEntity(request, entity);
        return flightRequestMapper.toDomain(flightRequestJpaRepository.save(entity));
    }

    /**
     * Ejecuta la operación find by id.
     * @param id variable de entrada id.
     * @return resultado de la operación find by id.
     */
    @Override
    public Optional<FlightRequest> findById(Long id) {
        return flightRequestJpaRepository.findById(id)
                .map(flightRequestMapper::toDomain);
    }

    /**
     * Ejecuta la operación find by flight.
     * @param flightDateUtc variable de entrada flightDateUtc.
     * @param airlineCode variable de entrada airlineCode.
     * @param originIata variable de entrada originIata.
     * @param destIata variable de entrada destIata.
     * @return resultado de la operación find by flight.
     */
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

    /**
     * Ejecuta la operación find by user id.
     * @param userId variable de entrada userId.
     * @return resultado de la operación find by user id.
     */
    @Override
    public List<FlightRequest> findByUserId(Long userId) {
        return flightRequestJpaRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(flightRequestMapper::toDomain)
                .collect(Collectors.toList());
    }

    /**
     * Ejecuta la operación find by ids.
     * @param ids variable de entrada ids.
     * @return resultado de la operación find by ids.
     */
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

    /**
     * Ejecuta la operación find by flight date between with user predictions.
     * @param start variable de entrada start.
     * @param end variable de entrada end.
     * @return resultado de la operación find by flight date between with user predictions.
     */
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

    /**
     * Ejecuta la operación find by flight date between without actuals.
     * @param start variable de entrada start.
     * @param end variable de entrada end.
     * @return resultado de la operación find by flight date between without actuals.
     */
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

    /**
     * Ejecuta la operación find active requests with flight date utc before.
     * @param cutoffUtc variable de entrada cutoffUtc.
     * @return resultado de la operación find active requests with flight date utc before.
     */
    @Override
    public List<FlightRequest> findActiveRequestsWithFlightDateUtcBefore(OffsetDateTime cutoffUtc) {
        return flightRequestJpaRepository.findActiveRequestsWithFlightDateUtcBefore(cutoffUtc)
                .stream()
                .map(flightRequestMapper::toDomain)
                .collect(Collectors.toList());
    }

    /**
     * Ejecuta la operación find by flight date before and active.
     * @param cutoff variable de entrada cutoff.
     * @return resultado de la operación find by flight date before and active.
     */
    @Override
    public List<FlightRequest> findByFlightDateBeforeAndActive(OffsetDateTime cutoff) {
        return flightRequestJpaRepository.findByFlightDateBeforeAndActive(cutoff)
                .stream()
                .map(flightRequestMapper::toDomain)
                .collect(Collectors.toList());
    }

    /**
     * Ejecuta la operación resolve entity.
     * @param id variable de entrada id.
     * @return resultado de la operación resolve entity.
     */

    private FlightRequestEntity resolveEntity(Long id) {
        if (id == null) {
            return new FlightRequestEntity();
        }
        return flightRequestJpaRepository.findById(id).orElseGet(FlightRequestEntity::new);
    }
}
