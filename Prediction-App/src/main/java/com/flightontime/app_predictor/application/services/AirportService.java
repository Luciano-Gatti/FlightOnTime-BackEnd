package com.flightontime.app_predictor.application.services;

import com.flightontime.app_predictor.application.dto.AirportDTO;
import com.flightontime.app_predictor.domain.model.Airport;
import com.flightontime.app_predictor.domain.ports.out.AirportInfoPort;
import com.flightontime.app_predictor.domain.ports.out.AirportRepositoryPort;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AirportService {
    private final AirportRepositoryPort airportRepositoryPort;
    private final AirportInfoPort airportInfoPort;

    public AirportService(AirportRepositoryPort airportRepositoryPort, AirportInfoPort airportInfoPort) {
        this.airportRepositoryPort = airportRepositoryPort;
        this.airportInfoPort = airportInfoPort;
    }

    public AirportDTO getAirportByIata(String airportIata) {
        String normalizedIata = normalizeIata(airportIata);
        Airport airport = airportRepositoryPort.findByIata(normalizedIata)
                .orElseGet(() -> fetchAndStoreAirport(normalizedIata));
        return AirportDTO.fromDomain(airport);
    }

    private Airport fetchAndStoreAirport(String airportIata) {
        Airport airport = airportInfoPort.findByIata(airportIata)
                .orElseThrow(() -> new AirportNotFoundException("Airport not found: " + airportIata));
        List<Airport> saved = airportRepositoryPort.saveAll(List.of(airport));
        return saved.isEmpty() ? airport : saved.get(0);
    }

    private String normalizeIata(String airportIata) {
        if (airportIata == null) {
            throw new IllegalArgumentException("iata must be length 3");
        }
        String normalized = airportIata.trim().toUpperCase();
        validateIata(normalized);
        return normalized;
    }

    private void validateIata(String airportIata) {
        if (airportIata == null || airportIata.length() != 3) {
            throw new IllegalArgumentException("iata must be length 3");
        }
    }

}
