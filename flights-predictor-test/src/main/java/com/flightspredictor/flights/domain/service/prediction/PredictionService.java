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
import com.flightspredictor.flights.domain.repository.FlightPredictionRepository;
import com.flightspredictor.flights.domain.util.GeoUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class PredictionService {

    private final AirportLookupService airportLookupService;
    private final PredictionApiClient predictionClient;
    private final PredictionMapper predictionMapper;
    private final RequestMapper requestMapper;
    private final FlightRequestRepository requestRepo;
    private final FlightPredictionRepository predictionRepo;

    public ModelPredictionResponse predict(PredictionRequest request){
        String correlationId = MDC.get("correlationId");
        log.info("PREDICT_STEP step=START correlationId={}", correlationId);

        // Trae los aeropuertos y sus coordenadas
        log.info("PREDICT_STEP step=RESOLVE_AIRPORTS correlationId={}", correlationId);
        var originAirport = airportLookupService.getAirport(request.origin());
        var destAirport = airportLookupService.getAirport(request.dest());

        // Busca si no existe una request en la base de datos
        log.info("PREDICT_STEP step=FIND_FLIGHT_REQUEST correlationId={}", correlationId);
        Optional<FlightRequest> existingRequest =
                requestRepo.
                        findByFlightDateUtcAndAirlineCodeAndOriginIataAndDestIata(
                                request.flightDateTime().toLocalDateTime(),
                                request.opUniqueCarrier(),
                                request.origin(),
                                request.dest()
            );

        // Si exite, devuelve la predicción asociada a request
        FlightRequest requestEntity = existingRequest.orElse(null);
        log.info("PREDICT_STEP step=FIND_FLIGHT_PREDICTION correlationId={}", correlationId);
        if (requestEntity != null) {
            List<FlightPrediction> predictions = requestEntity.getPredictions();
            if (predictions != null && !predictions.isEmpty()) {
                log.info("PREDICT_STEP step=END correlationId={}", correlationId);
                return new ModelPredictionResponse(predictions.get(0));
            }
        }

        double calculatedDistance;
        if (requestEntity != null) {
            calculatedDistance = requestEntity.getDistance();
        } else {
            // Calcula distancia automáticamente la distancia para inyectarla en la request
            calculatedDistance = GeoUtils.calculateDistance(
                    originAirport.getLatitude(), destAirport.getLatitude(),
                    originAirport.getLongitude(), destAirport.getLongitude()
            );
        }

        // Mapea la request para entregarla al modelo
        ModelPredictionRequest requestModel = requestMapper.mapToModelRequest(request, calculatedDistance);

        //Si no exite, hace la llamada al modelo
        PredictionResponse response = predictionClient.predict(requestModel);

        // Traduce la respuesta del modelo con los enums (prevision, status)
        ModelPredictionResponse domainResponse = predictionMapper.mapToModelResponse(response);

        // Construye las entidades para ser almacenadas en la base de datos
        if (requestEntity == null) {
            log.info("PREDICT_STEP step=CREATE_FLIGHT_REQUEST correlationId={}", correlationId);
            requestEntity = new FlightRequest(request, calculatedDistance);
            requestRepo.save(requestEntity);
        }
        FlightPrediction predictionEntity = new FlightPrediction(domainResponse, requestEntity);
        log.info("PREDICT_STEP step=CREATE_FLIGHT_PREDICTION correlationId={}", correlationId);
        predictionRepo.save(predictionEntity);

        log.info("PREDICT_STEP step=END correlationId={}", correlationId);

        return domainResponse;
    }
}






