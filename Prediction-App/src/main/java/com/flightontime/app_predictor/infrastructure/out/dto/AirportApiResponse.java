package com.flightontime.app_predictor.infrastructure.out.dto;

import java.util.List;

public record AirportApiResponse(List<AirportApiItem> data) {
}
