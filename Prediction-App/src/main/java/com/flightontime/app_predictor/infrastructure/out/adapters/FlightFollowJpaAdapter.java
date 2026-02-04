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

    /**
     * Ejecuta la operación flight follow jpa adapter.
     * @param flightSubscriptionJpaRepository variable de entrada flightSubscriptionJpaRepository.
     */

    /**
     * Ejecuta la operación flight follow jpa adapter.
     * @param flightSubscriptionJpaRepository variable de entrada flightSubscriptionJpaRepository.
     * @return resultado de la operación flight follow jpa adapter.
     */

    public FlightFollowJpaAdapter(FlightSubscriptionJpaRepository flightSubscriptionJpaRepository) {
        this.flightSubscriptionJpaRepository = flightSubscriptionJpaRepository;
    }

    /**
     * Ejecuta la operación save.
     * @param flightFollow variable de entrada flightFollow.
     * @return resultado de la operación save.
     */
    @Override
    public FlightFollow save(FlightFollow flightFollow) {
        if (flightFollow == null) {
            throw new IllegalArgumentException("Flight follow is required");
        }
        FlightSubscriptionEntity entity = resolveEntity(flightFollow.id());
        flightFollowMapper.toEntity(flightFollow, entity);
        return flightFollowMapper.toDomain(flightSubscriptionJpaRepository.save(entity));
    }

    /**
     * Ejecuta la operación find by user id and flight request id.
     * @param userId variable de entrada userId.
     * @param flightRequestId variable de entrada flightRequestId.
     * @return resultado de la operación find by user id and flight request id.
     */
    @Override
    public Optional<FlightFollow> findByUserIdAndFlightRequestId(Long userId, Long flightRequestId) {
        return flightSubscriptionJpaRepository.findFirstByUserIdAndFlightRequestId(userId, flightRequestId)
                .map(flightFollowMapper::toDomain);
    }

    /**
     * Ejecuta la operación find by refresh mode and flight date between.
     * @param refreshMode variable de entrada refreshMode.
     * @param start variable de entrada start.
     * @param end variable de entrada end.
     * @return resultado de la operación find by refresh mode and flight date between.
     */
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

    /**
     * Ejecuta la operación find by flight date between.
     * @param start variable de entrada start.
     * @param end variable de entrada end.
     * @return resultado de la operación find by flight date between.
     */
    @Override
    public List<FlightFollow> findByFlightDateBetween(OffsetDateTime start, OffsetDateTime end) {
        return flightSubscriptionJpaRepository.findByFlightDateBetween(start, end)
                .stream()
                .map(flightFollowMapper::toDomain)
                .collect(Collectors.toList());
    }

    /**
     * Ejecuta la operación resolve entity.
     * @param id variable de entrada id.
     * @return resultado de la operación resolve entity.
     */

    private FlightSubscriptionEntity resolveEntity(Long id) {
        if (id == null) {
            return new FlightSubscriptionEntity();
        }
        return flightSubscriptionJpaRepository.findById(id).orElseGet(FlightSubscriptionEntity::new);
    }
}
