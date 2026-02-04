package com.flightontime.app_predictor.domain.ports.out;

import com.flightontime.app_predictor.domain.model.FlightFollow;
import com.flightontime.app_predictor.domain.model.RefreshMode;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz FlightFollowRepositoryPort.
 */
public interface FlightFollowRepositoryPort {
    /**
     * Ejecuta la operación save.
     * @param flightFollow variable de entrada flightFollow.
     * @return resultado de la operación save.
     */
    FlightFollow save(FlightFollow flightFollow);

    /**
     * Ejecuta la operación find by user id and flight request id.
     * @param userId variable de entrada userId.
     * @param flightRequestId variable de entrada flightRequestId.
     * @return resultado de la operación find by user id and flight request id.
     */

    Optional<FlightFollow> findByUserIdAndFlightRequestId(Long userId, Long flightRequestId);

    /**
     * Ejecuta la operación find by refresh mode and flight date between.
     * @param refreshMode variable de entrada refreshMode.
     * @param start variable de entrada start.
     * @param end variable de entrada end.
     * @return resultado de la operación find by refresh mode and flight date between.
     */

    List<FlightFollow> findByRefreshModeAndFlightDateBetween(
            RefreshMode refreshMode,
            OffsetDateTime start,
            OffsetDateTime end
    );

    /**
     * Ejecuta la operación find by flight date between.
     * @param start variable de entrada start.
     * @param end variable de entrada end.
     * @return resultado de la operación find by flight date between.
     */

    List<FlightFollow> findByFlightDateBetween(OffsetDateTime start, OffsetDateTime end);
}
