package com.flightontime.app_predictor.domain.ports.out;

import java.util.List;

/**
 * Interfaz NotificationPort.
 */
public interface NotificationPort {
    /**
     * Ejecuta la operación send t12h summary.
     * @param userId variable de entrada userId.
     * @param messages variable de entrada messages.
     */
    void sendT12hSummary(Long userId, List<String> messages);
}
