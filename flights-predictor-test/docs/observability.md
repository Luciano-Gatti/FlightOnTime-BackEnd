Header de correlación: usar X-Correlation-Id en cada request.
Si no se envía, el backend genera un UUID y lo devuelve en la respuesta.
El valor queda disponible en logs bajo la clave MDC correlationId.
Ejemplo request: curl -H "X-Correlation-Id: 123e4567" http://localhost:8080/actuator/health
Ejemplo response header: X-Correlation-Id: 123e4567
Ejemplo log lookup (con app.debug.airport-lookup-trace=true):
AirportLookup correlationId=123e4567 iata=EZE source=DB caller=com.flightspredictor.flights.domain.service.prediction.PredictionService.predict
