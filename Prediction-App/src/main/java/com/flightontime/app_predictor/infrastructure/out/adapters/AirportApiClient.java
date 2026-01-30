package com.flightontime.app_predictor.infrastructure.out.adapters;

import com.flightontime.app_predictor.domain.model.Airport;
import com.flightontime.app_predictor.domain.ports.out.AirportInfoPort;
import com.flightontime.app_predictor.infrastructure.out.dto.AirportApiItem;
import com.flightontime.app_predictor.infrastructure.out.dto.AirportApiResponse;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class AirportApiClient implements AirportInfoPort {
    private final WebClient airportWebClient;

    public AirportApiClient(@Qualifier("airportWebClient") WebClient airportWebClient) {
        this.airportWebClient = airportWebClient;
    }

    @Override
    public Optional<Airport> findByIata(String airportIata) {
        AirportApiResponse response = airportWebClient.get()
                .uri(uriBuilder -> uriBuilder.path("/airports")
                        .queryParam("iata", airportIata)
                        .build())
                .retrieve()
                .bodyToMono(AirportApiResponse.class)
                .block();
        return toItems(response).stream()
                .findFirst()
                .map(this::toDomain);
    }

    @Override
    public List<Airport> searchByText(String text) {
        AirportApiResponse response = airportWebClient.get()
                .uri(uriBuilder -> uriBuilder.path("/airports/search")
                        .queryParam("q", text)
                        .build())
                .retrieve()
                .bodyToMono(AirportApiResponse.class)
                .block();
        return toItems(response).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private List<AirportApiItem> toItems(AirportApiResponse response) {
        if (response == null || response.data() == null) {
            return Collections.emptyList();
        }
        return response.data();
    }

    private Airport toDomain(AirportApiItem item) {
        Objects.requireNonNull(item, "Airport data is required");
        return new Airport(
                item.airportIata(),
                item.airportName(),
                item.country(),
                item.cityName(),
                item.latitude(),
                item.longitude(),
                item.elevation(),
                item.timeZone(),
                item.googleMaps()
        );
    }
}
