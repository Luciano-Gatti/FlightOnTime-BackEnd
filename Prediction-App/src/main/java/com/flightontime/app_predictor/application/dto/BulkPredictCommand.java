package com.flightontime.app_predictor.application.dto;

import java.util.List;

/**
 * Registro BulkPredictCommand.
 */
public record BulkPredictCommand(
        List<PredictCommand> predictCommands
) {
}
