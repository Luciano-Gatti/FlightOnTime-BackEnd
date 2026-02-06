package com.flightspredictor.flights.domain.validations;

import org.springframework.stereotype.Component;

import com.flightspredictor.flights.domain.entities.Airport;
import com.flightspredictor.flights.domain.dto.prediction.PredictionRequest;
import com.flightspredictor.flights.domain.error.BusinessErrorCodes;
import com.flightspredictor.flights.domain.error.BusinessException;

@Component
public class PredictionValidation {

    public void validarReglasDeNegocio(PredictionRequest dto){

        //Crea la validacion (Si origen es igual a destino, retorna el mensaje "El lugar de origen y el destino no pueden ser iguales")
        if (dto.getOrigin()!= null && dto.getDest()!= null && dto.getOrigin().equalsIgnoreCase(dto.getDest())){

                throw new BusinessException(
                        BusinessErrorCodes.INVALID_ROUTE,
                        "El lugar de origen y el destino no pueden ser iguales"
                );
        }
    }

    public void ValidarIata(Airport airport){
        //Crea la validación (Si el código IATA no se encuentra dentro de la base de datos, retorna el mensaje "El código IATA: 'xxx' no existe")
        if (airport == null) {
            throw new BusinessException(
                BusinessErrorCodes.INVALID_IATA,
                "El código IATA no existe"
            );
        }
    }
}
