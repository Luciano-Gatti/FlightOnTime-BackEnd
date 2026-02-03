# FlightOnTime-BackEnd
## FlightOnTime en breve
**Qué es FlightOnTime:** backend en Spring Boot que expone un API REST para predecir demoras de vuelos, consultar aeropuertos y obtener datos meteorológicos, con persistencia JPA de solicitudes y predicciones.  
**Qué problema resuelve:** ofrece predicciones anticipadas y consistentes para planificar mejor viajes y operaciones, evitando depender de consultas aisladas sin historial ni contexto.  
**Público objetivo:** usuarios finales (pasajeros que necesitan anticipar demoras) y aerolíneas/operaciones (equipos que requieren visibilidad de riesgo por vuelo).  
**Enfoque técnico:** integra un modelo de predicción de vuelos; ejecuta jobs programados (T-72h para refresco y T-12h para notificaciones); aplica cache por bucket temporal (ventanas fijas de horas) para reutilizar predicciones; admite importación masiva por CSV.  

Es una aplicación Spring Boot que expone un API REST para predecir demoras de vuelos, consultar aeropuertos y obtener datos meteorológicos. Está organizado en controladores y servicios, y usa persistencia con JPA para guardar solicitudes y predicciones cuando corresponde. 

## Formato CSV para importación masiva de predicciones
- **Header obligatorio**: `fl_date_utc,carrier,origin,dest,flight_number`
- **Campos**:
  - `fl_date_utc`: fecha/hora en UTC en formato ISO-8601 con offset obligatorio (ejemplo: `2026-02-01T18:30:00Z`).
  - `carrier`: código de aerolínea.
  - `origin`: aeropuerto de origen (IATA, 3 letras).
  - `dest`: aeropuerto de destino (IATA, 3 letras).
  - `flight_number`: número de vuelo (opcional, puede estar vacío).

Ejemplo de archivo CSV:
```
fl_date_utc,carrier,origin,dest,flight_number
2026-02-01T18:30:00Z,AA,JFK,MIA,100
2026-02-01T20:15:00Z,DL,LAX,SFO,
```
