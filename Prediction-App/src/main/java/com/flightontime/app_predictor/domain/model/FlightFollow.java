package com.flightontime.app_predictor.domain.model;

import java.time.OffsetDateTime;

/**
 * Registro FlightFollow.
 * @param id variable de entrada id.
 * @param userId variable de entrada userId.
 * @param flightRequestId variable de entrada flightRequestId.
 * @param refreshMode variable de entrada refreshMode.
 * @param baselineSnapshotId variable de entrada baselineSnapshotId.
 * @param createdAt variable de entrada createdAt.
 * @param updatedAt variable de entrada updatedAt.
 * @return resultado de la operación resultado.
 */
public record FlightFollow(
        Long id,
        Long userId,
        Long flightRequestId,
        RefreshMode refreshMode,
        Long baselineSnapshotId,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
