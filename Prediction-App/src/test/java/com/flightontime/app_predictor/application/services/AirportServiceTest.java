package com.flightontime.app_predictor.application.services;

import com.flightontime.app_predictor.application.dto.AirportDTO;
import com.flightontime.app_predictor.domain.exception.ExternalApiException;
import com.flightontime.app_predictor.domain.model.Airport;
import com.flightontime.app_predictor.domain.ports.out.AirportInfoPort;
import com.flightontime.app_predictor.domain.ports.out.AirportRepositoryPort;
import com.flightontime.app_predictor.infrastructure.out.http.ExternalProviderException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AirportServiceTest {

    @Mock
    private AirportRepositoryPort airportRepositoryPort;

    @Mock
    private AirportInfoPort airportInfoPort;

    private AirportService airportService;

    @BeforeEach
    void setUp() {
        airportService = new AirportService(airportRepositoryPort, airportInfoPort);
    }

    @Test
    void getAirportByIata_shouldUseNormalizedIataAndReturnFromRepository() {
        Airport airport = new Airport("JFK", "John F. Kennedy", "US", "New York",
                40.6, -73.7, 4.0, "America/New_York", "https://maps.google.com/?q=JFK");
        when(airportRepositoryPort.findByIata("JFK")).thenReturn(Optional.of(airport));

        AirportDTO result = airportService.getAirportByIata("  jfk ");

        assertEquals("JFK", result.airportIata());
        verify(airportRepositoryPort).findByIata("JFK");
        verify(airportInfoPort, never()).findByIata(any());
        verify(airportRepositoryPort, never()).save(any(Airport.class));
        verifyNoMoreInteractions(airportRepositoryPort, airportInfoPort);
    }

    @Test
    void getAirportByIata_shouldFetchFromProviderAndPersistWhenRepositoryMisses() {
        Airport providerAirport = new Airport("JFK", "John F. Kennedy", "US", "New York",
                40.6, -73.7, 4.0, "America/New_York", null);
        Airport persistedAirport = new Airport("JFK", "John F. Kennedy", "US", "New York",
                40.6, -73.7, 4.0, "America/New_York", "https://maps.google.com/?q=JFK");

        when(airportRepositoryPort.findByIata("JFK")).thenReturn(Optional.empty());
        when(airportInfoPort.findByIata("JFK")).thenReturn(Optional.of(providerAirport));
        when(airportRepositoryPort.save(any(Airport.class))).thenReturn(persistedAirport);

        AirportDTO result = airportService.getAirportByIata("jfk");

        assertEquals("JFK", result.airportIata());
        verify(airportRepositoryPort).findByIata("JFK");
        verify(airportInfoPort).findByIata("JFK");
        verify(airportRepositoryPort).save(providerAirport);
        verifyNoMoreInteractions(airportRepositoryPort, airportInfoPort);
    }

    @Test
    void getAirportByIata_shouldThrowAirportNotFoundWhenProviderReturnsEmpty() {
        when(airportRepositoryPort.findByIata("JFK")).thenReturn(Optional.empty());
        when(airportInfoPort.findByIata("JFK")).thenReturn(Optional.empty());

        assertThrows(AirportNotFoundException.class, () -> airportService.getAirportByIata("JFK"));

        verify(airportRepositoryPort).findByIata("JFK");
        verify(airportInfoPort).findByIata("JFK");
        verify(airportRepositoryPort, never()).save(any(Airport.class));
        verifyNoMoreInteractions(airportRepositoryPort, airportInfoPort);
    }

    @Test
    void getAirportByIata_shouldWrapProviderFailuresAsExternalApiException() {
        when(airportRepositoryPort.findByIata("JFK")).thenReturn(Optional.empty());
        when(airportInfoPort.findByIata("JFK")).thenThrow(new ExternalProviderException(
                "airport-api",
                503,
                "unavailable",
                null,
                new RuntimeException("timeout")
        ));

        assertThrows(ExternalApiException.class, () -> airportService.getAirportByIata("JFK"));

        verify(airportRepositoryPort).findByIata("JFK");
        verify(airportInfoPort).findByIata("JFK");
        verify(airportRepositoryPort, never()).save(any(Airport.class));
        verifyNoMoreInteractions(airportRepositoryPort, airportInfoPort);
    }
}
