package com.flightontime.app_predictor.application.dto;

import java.util.List;

public record BulkPredictCommand(
        List<PredictCommand> predictCommands
) {
}
