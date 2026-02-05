# Execution Map (backend)

## 1) Endpoints (Controller -> método) y cadena de ejecución

> Alcance: `Prediction-App/src/main/java/com/flightontime/app_predictor`.

| Endpoint | Controller#método | Service/UseCase directo invocado | Puertos OUT usados (transitivos) | Adapters concretos |
|---|---|---|---|---|
| `POST /predict` | `PredictController#predict` | `PredictFlightUseCase` → `PredictFlightService#predict` | `FlightRequestRepositoryPort`, `FlightFollowRepositoryPort`, `UserPredictionRepositoryPort` + (vía `PredictionWorkflowService`) `ModelPredictionPort`, `DistanceUseCase` (`DistanceService`), `PredictionRepositoryPort` | `FlightRequestJpaAdapter`, `FlightFollowJpaAdapter`, `UserPredictionJpaAdapter`, `FastApiModelClient`, `PredictionJpaAdapter`, `AirportJpaAdapter`, `AirportApiClient` |
| `POST /predict/bulk-import` | `PredictController#bulkImport` | `BulkPredictUseCase` → `BulkPredictService#importPredictionsFromCsv` | `FlightFollowRepositoryPort`, `UserPredictionRepositoryPort` + (vía `PredictionWorkflowService`) `ModelPredictionPort`, `FlightRequestRepositoryPort`, `PredictionRepositoryPort`, `DistanceUseCase` | `FlightFollowJpaAdapter`, `UserPredictionJpaAdapter`, `FastApiModelClient`, `FlightRequestJpaAdapter`, `PredictionJpaAdapter`, `AirportJpaAdapter`, `AirportApiClient` |
| `GET /predict/history` | `PredictController#getHistory` | `PredictHistoryUseCase` → `PredictHistoryService#getHistory` | `FlightRequestRepositoryPort`, `PredictionRepositoryPort`, `UserPredictionRepositoryPort` | `FlightRequestJpaAdapter`, `PredictionJpaAdapter`, `UserPredictionJpaAdapter` |
| `GET /predict/history/{requestId}` | `PredictController#getHistoryDetail` | `PredictHistoryUseCase` → `PredictHistoryService#getHistoryDetail` | `FlightRequestRepositoryPort`, `PredictionRepositoryPort`, `UserPredictionRepositoryPort` | `FlightRequestJpaAdapter`, `PredictionJpaAdapter`, `UserPredictionJpaAdapter` |
| `GET /predict/{requestId}/latest` | `PredictController#getLatest` | `PredictFlightUseCase` → `PredictFlightService#getLatestPrediction` | `FlightRequestRepositoryPort`, `PredictionRepositoryPort`, `UserPredictionRepositoryPort` | `FlightRequestJpaAdapter`, `PredictionJpaAdapter`, `UserPredictionJpaAdapter` |
| `GET /airports/{iata}` | `AirportController#getAirport` | `AirportService#getAirportByIata` | `AirportRepositoryPort`, `AirportInfoPort` | `AirportJpaAdapter`, `AirportApiClient` |
| `GET /airports/{iata}/weather` | `WeatherController#getWeather` | `WeatherService#getCurrentWeather` | `WeatherPrimaryProviderPort`, `WeatherFallbackProviderPort`, `AirportRepositoryPort`, `AirportInfoPort` | `OpenMeteoClient`, `WeatherApiFallbackProviderClient`, `AirportJpaAdapter`, `AirportApiClient` |
| `GET /stats/summary` | `StatsController#getSummary` | `StatsSummaryUseCase` → `StatsSummaryService#getSummary` | `PredictionRepositoryPort`, `FlightActualRepositoryPort`, `UserPredictionRepositoryPort`, `FlightRequestRepositoryPort` | `PredictionJpaAdapter`, `FlightActualJpaAdapter`, `UserPredictionJpaAdapter`, `FlightRequestJpaAdapter` |
| `GET /stats/accuracy-by-leadtime` | `StatsController#getAccuracyByLeadTime` | `StatsAccuracyByLeadTimeUseCase` → `StatsAccuracyByLeadTimeService#getAccuracyByLeadTime` | `PredictionRepositoryPort` | `PredictionJpaAdapter` |
| `POST /auth/register` | `AuthController#register` | `AuthService#register` | `UserRepositoryPort`, `PasswordHasherPort` | `UserJpaAdapter`, `PasswordEncoderAdapter` |
| `POST /auth/login` | `AuthController#login` | `AuthService#login` | `UserRepositoryPort`, `PasswordHasherPort` | `UserJpaAdapter`, `PasswordEncoderAdapter` |

---

## 2) Flujos importantes

## 2.1 Predict single (`POST /predict`)
1. `PredictController#predict` valida DTO y resuelve `userId`.
2. Llama `PredictFlightService#predict`.
3. `PredictFlightService` normaliza/valida y delega a `PredictionWorkflowService#predict`.
4. `PredictionWorkflowService`:
   - calcula distancia con `DistanceService` (usa `AirportRepositoryPort` y fallback `AirportInfoPort`),
   - busca/crea `FlightRequest` en `FlightRequestRepositoryPort`,
   - resuelve predicción por bucket en `PredictionRepositoryPort` o llama modelo vía `ModelPredictionPort`.
5. Si hay `userId`, `PredictFlightService` guarda snapshot en `UserPredictionRepositoryPort` y upsert de seguimiento en `FlightFollowRepositoryPort`.

**Adapters clave**: `FastApiModelClient`, `FlightRequestJpaAdapter`, `PredictionJpaAdapter`, `UserPredictionJpaAdapter`, `FlightFollowJpaAdapter`, `AirportJpaAdapter`, `AirportApiClient`.

## 2.2 Predict history (`GET /predict/history` y `GET /predict/history/{requestId}`)
1. `PredictController` obtiene `userId` autenticado.
2. `PredictHistoryService#getHistory`:
   - trae requests del usuario (`FlightRequestRepositoryPort#findByUserId`),
   - trae predicciones por request/usuario (`PredictionRepositoryPort#findByRequestIdAndUserId`),
   - calcula usuarios únicos por request (`UserPredictionRepositoryPort#countDistinctUsersByRequestId`).
3. `PredictHistoryService#getHistoryDetail` valida ownership/visibilidad con snapshot del usuario y devuelve timeline ordenado.

**Adapters clave**: `FlightRequestJpaAdapter`, `PredictionJpaAdapter`, `UserPredictionJpaAdapter`.

## 2.3 Bulk import (`POST /predict/bulk-import`)
1. `PredictController#bulkImport` valida archivo CSV y deriva a `BulkPredictService#importPredictionsFromCsv`.
2. `BulkPredictService` parsea (`CsvParser`), valida filas (`BulkPredictRowValidator`) y por cada fila válida ejecuta `PredictionWorkflowService#predict`.
3. Para usuario autenticado, guarda snapshot (`UserPredictionRepositoryPort`) y seguimiento T12 (`FlightFollowRepositoryPort`).
4. Devuelve conteos `accepted/rejected` y lista de errores por fila.

**Adapters clave**: mismos del flujo de predicción + persistencia de seguimiento (`FlightFollowJpaAdapter`).

## 2.4 Weather lookup (`GET /airports/{iata}/weather`)
1. `WeatherController#getWeather` valida IATA y timestamp UTC.
2. `WeatherService#getCurrentWeather`:
   - intenta resolver aeropuerto en local (`AirportRepositoryPort`),
   - si no existe, consulta externo (`AirportInfoPort`) y persiste,
   - consulta clima primario (`WeatherPrimaryProviderPort`, OpenMeteo),
   - si falla, fallback (`WeatherFallbackProviderPort`).
3. Cache en memoria por IATA con TTL (`weather.cache.ttl`).

**Adapters clave**: `AirportJpaAdapter`, `AirportApiClient`, `OpenMeteoClient`, `WeatherApiFallbackProviderClient`.

## 2.5 Airport lookup (`GET /airports/{iata}`)
1. `AirportController#getAirport` normaliza IATA.
2. `AirportService#getAirportByIata` busca en DB local (`AirportRepositoryPort`).
3. Si no existe, consulta externo (`AirportInfoPort`) y guarda local.

**Adapters clave**: `AirportJpaAdapter`, `AirportApiClient`.

## 2.6 Flight actual lookup (sin endpoint HTTP directo)
Se ejecuta desde job programado:
1. `ActualsFetchSchedulerConfig#fetchActuals` dispara `ActualsFetchJobService#fetchActuals`.
2. El job busca requests activos vencidos (`FlightRequestRepositoryPort`).
3. Para cada request intenta recuperar actual (`FlightActualPort`):
   - por `flightNumber`+fecha, o
   - por ruta+ventana horaria.
4. Guarda resultado en `FlightActualRepositoryPort` y puede desactivar request según estado final.

**Adapters clave**: `AeroDataBoxFlightActualClient`, `FlightActualJpaAdapter`, `FlightRequestJpaAdapter`.

## 2.7 Stats (`GET /stats/*`)
### a) Summary (`/stats/summary`)
- `StatsSummaryService#getSummary` compone métricas agregadas usando:
  - `PredictionRepositoryPort` (totales y estados predichos),
  - `FlightActualRepositoryPort` (actuales/cancelados),
  - `UserPredictionRepositoryPort` + `FlightRequestRepositoryPort` (top vuelos por usuarios únicos).

### b) Accuracy by lead time (`/stats/accuracy-by-leadtime`)
- `StatsAccuracyByLeadTimeService#getAccuracyByLeadTime` usa `PredictionRepositoryPort#findAccuracySamplesExcludingCancelled` y agrupa en bins de 3h (hasta 72h+).

**Adapters clave**: `PredictionJpaAdapter`, `FlightActualJpaAdapter`, `UserPredictionJpaAdapter`, `FlightRequestJpaAdapter`.

---

## 3) Mapa de puertos OUT -> adapters concretos

| Puerto OUT | Adapter concreto |
|---|---|
| `FlightRequestRepositoryPort` | `FlightRequestJpaAdapter` |
| `PredictionRepositoryPort` | `PredictionJpaAdapter` |
| `UserPredictionRepositoryPort` | `UserPredictionJpaAdapter` |
| `FlightFollowRepositoryPort` | `FlightFollowJpaAdapter` |
| `FlightActualRepositoryPort` | `FlightActualJpaAdapter` |
| `UserRepositoryPort` | `UserJpaAdapter` |
| `AirportRepositoryPort` | `AirportJpaAdapter` |
| `NotificationLogRepositoryPort` | `NotificationLogJpaAdapter` |
| `PasswordHasherPort` | `PasswordEncoderAdapter` |
| `ModelPredictionPort` | `FastApiModelClient` |
| `AirportInfoPort` | `AirportApiClient` |
| `WeatherPrimaryProviderPort` | `OpenMeteoClient` |
| `WeatherFallbackProviderPort` | `WeatherApiFallbackProviderClient` |
| `FlightActualPort` | `AeroDataBoxFlightActualClient` |
| `NotificationPort` | `NotificationAdapter` |

---

## 4) Jobs/schedulers y ejecución al startup

### Schedulers detectados (`@Scheduled`)
- `T12hNotifySchedulerConfig#notifyUsers` → corre cada `PT1H` y ejecuta `T12hNotifyJobService#notifyUsers`.
- `T72hRefreshSchedulerConfig#refreshPredictions` → corre cada `PT3H` y ejecuta `T72hRefreshJobService#refreshPredictions`.
- `ActualsFetchSchedulerConfig#fetchActuals` → cron `0 59 11,23 * * *` (`America/Argentina/Buenos_Aires`) y ejecuta `ActualsFetchJobService#fetchActuals`.

### Startup hooks detectados
- No se detectaron clases con `CommandLineRunner`, `ApplicationRunner`, `@PostConstruct` ni listeners de `ApplicationReadyEvent` en el código de aplicación.
