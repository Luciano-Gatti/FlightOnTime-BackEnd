package com.flightontime.app_predictor.infrastructure.out.adapters;

import com.flightontime.app_predictor.domain.model.Airport;
import com.flightontime.app_predictor.domain.ports.out.AirportInfoPort;
import com.flightontime.app_predictor.infrastructure.out.dto.AirportApiResponse;
import com.flightontime.app_predictor.infrastructure.out.dto.AirportApiSearchResponse;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
public class AirportApiClient implements AirportInfoPort {
    private final WebClient airportWebClient;

    public AirportApiClient(@Qualifier("airportWebClient") WebClient airportWebClient) {
        this.airportWebClient = airportWebClient;
    }

    @Override
    public Optional<Airport> findByIata(String airportIata) {
        try {
            AirportApiResponse response = airportWebClient.get()
                    .uri("/airports/{iata}", airportIata)
                    .retrieve()
                    .bodyToMono(AirportApiResponse.class)
                    .block();
            return Optional.ofNullable(response).map(this::toDomain);
        } catch (WebClientResponseException.NotFound ex) {
            return Optional.empty();
        }
    }

    @Override
    public List<Airport> searchByText(String text) {
        try {
            AirportApiSearchResponse response = airportWebClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/airports")
                            .queryParam("search", text)
                            .build())
                    .retrieve()
                    .bodyToMono(AirportApiSearchResponse.class)
                    .block();
            if (response == null || response.data() == null) {
                return Collections.emptyList();
            }
            return response.data().stream()
                    .map(this::toDomain)
                    .collect(Collectors.toList());
        } catch (WebClientResponseException.NotFound ex) {
            return Collections.emptyList();
        }
    }

    private Airport toDomain(AirportApiResponse response) {
        return new Airport(
                response.airportIata(),
                response.airportName(),
                response.country(),
                response.cityName(),
                response.latitude(),
                response.longitude(),
                response.elevation(),
                response.timeZone(),
                response.googleMaps()
        );
    }
}
