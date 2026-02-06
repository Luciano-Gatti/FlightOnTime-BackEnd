# GAP ANALYSIS - Backend Flight Predictor

## A) Requerimientos (1-12)

| # | Requerimiento | Estado | Evidencia (paths) | Qué falta exactamente | Riesgo/Impacto |
| --- | --- | --- | --- | --- | --- |
| 1 | Autenticación JWT + roles | **Falta** | `pom.xml` (Spring Security comentado), `infra/security/TestSecurity.java` | No hay dependencias de security activas, no hay configuración de filtros JWT, ni roles/autorización por endpoint. | **Alto** (API expuesta sin auth) |
| 2 | Swagger/OpenAPI con botón Authorize + bearer JWT | **Parcial** | `pom.xml` (springdoc), `infra/springdoc/TestDoc.java` | Hay dependencia OpenAPI, pero no configuración de `SecurityScheme`/`SecurityRequirement`, ni integración con JWT; no evidencia de swagger UI configurado. | **Medio** (documentación incompleta, no prueba auth) |
| 3 | Endpoint predict (validaciones + pipeline features) | **Parcial** | `controller/PredictionController.java`, `domain/dto/prediction/PredictionRequest.java`, `domain/service/prediction/PredictionService.java`, `domain/mapper/prediction/RequestMapper.java`, `domain/util/GeoUtils.java` | Validaciones básicas en DTO OK, pero reglas de negocio (origen=destino, IATA existente) existen en `PredictionValidation` y no se aplican. Pipeline de features genera `distance` y algunos campos derivados, pero el resto de features se rellenan con ceros en `RequestMapper`. | **Medio** (errores de negocio sin validar y features incompletas) |
| 4 | Flujo aeropuerto (BD → proveedor externo → persistir, fix LAX) | **Parcial** | `domain/service/airports/AirportService.java`, `domain/service/prediction/AirportLookupService.java`, `infra/external/airports/client/AirportApiClient.java` | Existe lookup en BD + fallback externo y persistencia, pero no hay tratamiento explícito del caso “LAX” ni lógica específica de corrección. | **Medio** (casos especiales no cubiertos) |
| 5 | Flujo clima (consulta externa + timeouts + fallos) | **OK** | `infra/external/weather/client/WeatherApiClient.java`, `infra/external/weather/client/WeatherFallbackClient.java`, `infra/external/weather/service/WeatherService.java` | Timeouts configurados y fallback implementado, con manejo de errores y retries básicos. | **Bajo** |
| 6 | Jobs T-72H y T-12H (batch, logs, evitar duplicados) | **Falta** | (sin evidencia) | No hay `@Scheduled`, batch, ni repos/entidades para jobs. | **Alto** |
| 7 | Carga masiva CSV (lotes, estados, procesamiento) | **Falta** | (sin evidencia) | No hay endpoints/servicios para carga ni modelos de batch/lote. | **Alto** |
| 8 | Historial (paginado, filtros) | **Falta** | (sin evidencia) | No hay endpoints ni queries con paginación/filtros. | **Medio** |
| 9 | Stats/health (si existen) | **Parcial** | `controller/PredictionStatsController.java`, `domain/service/prediction/PredictionStatsService.java`, `pom.xml` (actuator) | Stats básicos existen; health depende de Actuator pero no está documentado/validado ni configurado. | **Bajo** |
| 10 | Manejo de errores estandar (HTTP + payload) | **Parcial** | `infra/error/GlobalExceptionHandler.java`, `controller/WeatherController.java` | Existe handler global para validaciones/BusinessException, pero endpoints como Weather manejan errores manualmente con payloads inconsistentes. | **Medio** |
| 11 | Logs con correlationId (seguimiento end-to-end) | **Falta** | (sin evidencia) | No hay filtros/interceptores para inyectar o propagar `correlationId` en logs. | **Medio** |
| 12 | Tests (unit/integration) mínimos para lo crítico | **Parcial** | `src/test/java/...` | Hay unit tests para mappers, DTOs y `PredictionService`, pero no hay integración (controllers, repos, external clients) ni cobertura para aeropuertos, clima, seguridad, jobs, CSV, historial. | **Medio** |

---

## B) Flyway & DB Consistency

### Migraciones encontradas (orden)
1. `V1__create-table_airports.sql`
2. `V2__create-table-request.sql`
3. `V3__create-table-prediction.sql`
4. `V4__add_resultado_real_column.sql`
5. `V5__alter-prediction-status-column.sql`

### Problemas detectados
- **Mismatch 1: prediction.request_id**
  - En SQL, `request_id` es nullable y no tiene constraint unique.
  - En JPA, `@OneToOne` con `nullable = false` y `unique = true`.
  - Riesgo: inconsistencia de cardinalidad (DB permite múltiples `prediction` para un mismo `request`).

- **Mismatch 2: columna `resultado_real`**
  - Agregada en `V4__add_resultado_real_column.sql`, pero no existe en la entidad `Prediction`.
  - Riesgo: datos no mapeados ni accesibles en el modelo.

- **Índices ausentes para búsquedas frecuentes**
  - `airports.airport_iata` no tiene índice/unique constraint, aunque se consulta por IATA.
  - `request` tiene constraint `unique` en varias columnas, pero no índices explícitos para queries futuras de historial.

- **Tipos y precisión**
  - `latitude/longitude` en SQL son `decimal(10,4)` y en JPA `Double`: puede haber pérdida de precisión si el proveedor entrega más decimales.
  - `fl_date` en SQL es `timestamp` y en JPA `OffsetDateTime`: potencial desajuste de zona horaria si no se configura Hibernate.

### Mismatch Entity/Column
- `Prediction` ↔ `prediction.request_id`: nullability + unique inconsistente.
- `Prediction` ↔ `prediction.resultado_real`: columna no mapeada.
- `Airport` ↔ `airports.airport_iata`: falta índice/unique si se espera consulta por IATA.

### Recomendaciones (sin implementar)
- Alinear `request_id` en SQL con la relación `@OneToOne` (NOT NULL + UNIQUE) o ajustar el mapping.
- Incorporar la columna `resultado_real` en la entidad `Prediction` (si es parte del dominio).
- Agregar índice/unique a `airports.airport_iata`.
- Revisar configuración de timezone para `OffsetDateTime` en JPA.

---

## C) Plan de implementación recomendado (MVP primero)

1) **Seguridad + OpenAPI**
   - **Módulo:** `infra/security`, `infra/springdoc`.
   - **Acciones:** activar Spring Security, JWT filter, roles, y agregar `SecurityScheme` en OpenAPI.
   - **Tests mínimos:** integración de endpoints protegidos + swagger UI con auth.

2) **Predict + Validaciones de negocio**
   - **Módulo:** `domain/validations`, `domain/service/prediction`, `controller`.
   - **Acciones:** aplicar `PredictionValidation` (origen != destino, IATA existe), completar features del modelo.
   - **Tests mínimos:** unit tests de validaciones + test del endpoint con validaciones.

3) **Aeropuertos + Fix LAX**
   - **Módulo:** `domain/service/airports`, `infra/external/airports`.
   - **Acciones:** revisar fallback de API y crear corrección específica para LAX si aplica.
   - **Tests mínimos:** integración con repos y mock de API externa.

4) **Clima resiliente**
   - **Módulo:** `infra/external/weather`.
   - **Acciones:** revisar configuración de timeouts globales y manejo consistente de errores.
   - **Tests mínimos:** unit tests de fallback y parsing.

5) **Historia/Stats/Health**
   - **Módulo:** `controller`, `domain/service`, `domain/repository`.
   - **Acciones:** agregar endpoints de historial con paginación/filtros; definir health checks relevantes.
   - **Tests mínimos:** tests de repos con paginación y filtro.

6) **Jobs T-72H y T-12H**
   - **Módulo:** `domain/service`, `infra`.
   - **Acciones:** diseñar batch con control de duplicados y logs.
   - **Tests mínimos:** unit tests de scheduling y deduplicación.

7) **Carga masiva CSV**
   - **Módulo:** `controller`, `domain/service`, `domain/entities`.
   - **Acciones:** definir entidad de batch, estados, y lógica de procesamiento.
   - **Tests mínimos:** parser CSV + workflow de estados.

8) **Errores estandarizados + correlationId**
   - **Módulo:** `infra/error`, `infra`.
   - **Acciones:** unificar payloads, agregar filtro `correlationId` y logging MDC.
   - **Tests mínimos:** tests de handler global y filtro.
