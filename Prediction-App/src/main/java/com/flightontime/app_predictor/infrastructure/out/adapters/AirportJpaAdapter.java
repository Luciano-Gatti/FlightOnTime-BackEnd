package com.flightontime.app_predictor.infrastructure.out.adapters;

import com.flightontime.app_predictor.domain.model.Airport;
import com.flightontime.app_predictor.domain.ports.out.AirportRepositoryPort;
import com.flightontime.app_predictor.infrastructure.out.entities.AirportEntity;
import com.flightontime.app_predictor.infrastructure.out.repository.AirportJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class AirportJpaAdapter implements AirportRepositoryPort {
    private final AirportJpaRepository airportJpaRepository;

    public AirportJpaAdapter(AirportJpaRepository airportJpaRepository) {
        this.airportJpaRepository = airportJpaRepository;
    }

    @Override
    public Optional<Airport> findByIata(String airportIata) {
        return airportJpaRepository.findByAirportIata(airportIata)
                .map(this::toDomain);
    }

    @Override
    public List<Airport> saveAll(List<Airport> airports) {
        List<AirportEntity> entities = airports.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
        return airportJpaRepository.saveAll(entities).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private Airport toDomain(AirportEntity entity) {
        return new Airport(
                entity.getAirportIata(),
                entity.getAirportName(),
                entity.getCountry(),
                entity.getCityName(),
                entity.getLatitude(),
                entity.getLongitude(),
                entity.getElevation(),
                entity.getTimeZone(),
                entity.getGoogleMaps()
        );
    }

    private AirportEntity toEntity(Airport airport) {
        AirportEntity entity = new AirportEntity();
        entity.setAirportIata(airport.airportIata());
        entity.setAirportName(airport.airportName());
        entity.setCountry(airport.country());
        entity.setCityName(airport.cityName());
        entity.setLatitude(airport.latitude());
        entity.setLongitude(airport.longitude());
        entity.setElevation(airport.elevation());
        entity.setTimeZone(airport.timeZone());
        entity.setGoogleMaps(airport.googleMaps());
        return entity;
    }
}
