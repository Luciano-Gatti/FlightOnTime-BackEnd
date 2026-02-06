package com.flightspredictor.flights.domain.service.prediction;

import com.flightspredictor.flights.infra.external.prediction.client.PredictionApiClient;
import com.flightspredictor.flights.domain.dto.prediction.ModelPredictionRequest;
import com.flightspredictor.flights.domain.dto.prediction.ModelPredictionResponse;

import java.util.List;
import java.util.Optional;

import com.flightspredictor.flights.domain.dto.prediction.PredictionRequest;
import com.flightspredictor.flights.domain.dto.prediction.PredictionResponse;
import com.flightspredictor.flights.domain.entities.FlightPrediction;
import com.flightspredictor.flights.domain.entities.FlightRequest;
import com.flightspredictor.flights.domain.mapper.prediction.PredictionMapper;
import com.flightspredictor.flights.domain.mapper.prediction.RequestMapper;
import com.flightspredictor.flights.domain.repository.FlightRequestRepository;
import com.flightspredictor.flights.domain.util.GeoUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class PredictionService {

    private final AirportLookupService airportLookupService;
    private final PredictionApiClient predictionClient;
    private final PredictionMapper predictionMapper;
    private final RequestMapper requestMapper;
    private final FlightRequestRepository requestRepo;

    public ModelPredictionResponse predict(PredictionRequest request){

        // Busca los aeropueros desde la request para guardarlos en base de datos si no existen
        airportLookupService.getAirportExist(
                request.origin(),
                request.dest()
        );

        // Trae los aeropuertos y sus coordenadas
        var originAirport = airportLookupService.getAirport(request.origin());
        var destAirport = airportLookupService.getAirport(request.dest());

        // Calcula distancia automáticamente la distancia para inyectarla en la request
        double calculatedDistance = GeoUtils.calculateDistance(
                originAirport.get().getLatitude(), destAirport.get().getLatitude(),
                originAirport.get().getLongitude(), destAirport.get().getLongitude()
        );

        // Busca si no existe una request en la base de datos
        Optional<FlightRequest> existingRequest =
                requestRepo.
                        findByFlightDateUtcAndAirlineCodeAndOriginIataAndDestIataAndDistance(
                                request.flightDateTime().toLocalDateTime(),
                                request.opUniqueCarrier(),
                                request.origin(),
                                request.dest(),
                                calculatedDistance
            );

        // Si exite, devuelve la predicción asociada a request
        FlightRequest requestEntity = existingRequest.orElse(null);
        if (requestEntity != null) {
            List<FlightPrediction> predictions = requestEntity.getPredictions();
            if (predictions != null && !predictions.isEmpty()) {
                return new ModelPredictionResponse(predictions.get(0));
            }
        }

        // Mapea la request para entregarla al modelo
        ModelPredictionRequest requestModel = requestMapper.mapToModelRequest(request, calculatedDistance);

        //Si no exite, hace la llamada al modelo
        PredictionResponse response = predictionClient.predict(requestModel);

        // Traduce la respuesta del modelo con los enums (prevision, status)
        ModelPredictionResponse domainResponse = predictionMapper.mapToModelResponse(response);

        // Construye las entidades para ser almacenadas en la base de datos
        if (requestEntity == null) {
            requestEntity = new FlightRequest(request, calculatedDistance);
        }
        FlightPrediction predictionEntity = new FlightPrediction(domainResponse, requestEntity);
        requestEntity.setPredictions(List.of(predictionEntity));

        // Guarda la request y a la vez la prediction asociada a ella
        requestRepo.save(requestEntity);

        return domainResponse;
    }
}










