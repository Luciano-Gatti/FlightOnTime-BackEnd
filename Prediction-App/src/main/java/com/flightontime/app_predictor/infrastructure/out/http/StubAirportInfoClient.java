package com.flightontime.app_predictor.infrastructure.out.http;

import com.flightontime.app_predictor.domain.model.Airport;
import com.flightontime.app_predictor.domain.ports.out.AirportInfoPort;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Stub de aeropuertos para desarrollo local.
 */
@Component
@ConditionalOnProperty(name = "providers.stub", havingValue = "true")
public class StubAirportInfoClient implements AirportInfoPort {

    private static final Map<String, Airport> KNOWN_AIRPORTS = Map.of(
            "EZE", new Airport("EZE", "Ministro Pistarini", "AR", "Buenos Aires", -34.8222, -58.5358, 20.0,
                    "America/Argentina/Buenos_Aires", "https://maps.google.com/?q=EZE"),
            "AEP", new Airport("AEP", "Jorge Newbery", "AR", "Buenos Aires", -34.5592, -58.4156, 5.0,
                    "America/Argentina/Buenos_Aires", "https://maps.google.com/?q=AEP"),
            "SCL", new Airport("SCL", "Arturo Merino Benitez", "CL", "Santiago", -33.3929, -70.7858, 474.0,
                    "America/Santiago", "https://maps.google.com/?q=SCL"),
            "MIA", new Airport("MIA", "Miami International", "US", "Miami", 25.7959, -80.2870, 2.0,
                    "America/New_York", "https://maps.google.com/?q=MIA")
    );

    @Override
    public Optional<Airport> findByIata(String airportIata) {
        if (airportIata == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(KNOWN_AIRPORTS.get(airportIata.trim().toUpperCase()));
    }

    @Override
    public List<Airport> searchByText(String text) {
        return List.of();
    }
}
