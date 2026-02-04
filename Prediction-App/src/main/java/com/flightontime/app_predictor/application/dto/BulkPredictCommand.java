package com.flightontime.app_predictor.application.dto;

import java.util.List;

/**
 * Registro BulkPredictCommand.
 * @param predictCommands variable de entrada predictCommands.
 * @return resultado de la operación resultado.
 */
public record BulkPredictCommand(
        List<PredictCommand> predictCommands
) {
}
