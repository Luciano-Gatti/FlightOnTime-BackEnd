package com.flightontime.app_predictor.infrastructure.out.adapters;

import com.flightontime.app_predictor.domain.model.Airport;
import com.flightontime.app_predictor.domain.ports.out.AirportInfoPort;
import com.flightontime.app_predictor.infrastructure.out.dto.AirportApiResponse;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
public class AirportApiClient implements AirportInfoPort {
    private final WebClient airportWebClient;
    private final String apiKey;

    public AirportApiClient(
            @Qualifier("airportWebClient") WebClient airportWebClient,
            @Value("${api.market.key:}") String apiKey
    ) {
        this.airportWebClient = airportWebClient;
        this.apiKey = apiKey;
    }

    @Override
    public Optional<Airport> findByIata(String airportIata) {
        try {
            AirportApiResponse response = airportWebClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/airports/Iata/{iata}")
                            .queryParam("withRunways", "false")
                            .queryParam("withTime", "false")
                            .build(airportIata))
                    .header("x-api-market-key", apiKey)
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
        return Collections.emptyList();
    }

    private Airport toDomain(AirportApiResponse response) {
        return new Airport(
                response.airportIata(),
                response.airportName(),
                response.country() != null ? response.country().name() : null,
                response.cityName(),
                response.location() != null ? response.location().lat() : null,
                response.location() != null ? response.location().lon() : null,
                response.elevation() != null ? response.elevation().meter() : null,
                response.timeZone(),
                response.googleMaps() != null ? response.googleMaps().googleMaps() : null
        );
    }
}
