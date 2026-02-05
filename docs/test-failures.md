# Test Failures Report

## Alcance y comandos ejecutados
Se ejecutó la suite de tests en ambos módulos Maven del repositorio:

1. `Prediction-App`: `mvn -q test`
2. `flights-predictor-test`: `mvn -q test`

---

## Resultado global
No se llegó a ejecutar ningún test unitario/integración porque ambos módulos fallan **antes** de la fase de test, durante la resolución del parent POM en Maven.

---

## Fallas detectadas

## 1) Módulo `Prediction-App`
- **Estado:** fallo en inicialización del build (pre-test).
- **Tests fallando:** no disponible (ninguno ejecutado).
- **Error principal:**
  - `Non-resolvable parent POM for com.flightontime:app_predictor:0.0.1-SNAPSHOT`
  - No se pudo resolver `org.springframework.boot:spring-boot-starter-parent:pom:4.0.2`
  - Respuesta de Maven Central: `403 Forbidden`
- **Causa raíz resumida:** dependencia padre de Maven inaccesible desde el entorno actual; el proyecto no puede construirse y por eso no inicia la ejecución de tests.
- **Clasificación:** **a) wiring/context** (fallo de contexto de build/arranque de entorno de test; no llega a levantar contexto Spring).

## 2) Módulo `flights-predictor-test`
- **Estado:** fallo en inicialización del build (pre-test).
- **Tests fallando:** no disponible (ninguno ejecutado).
- **Error principal:**
  - `Non-resolvable parent POM for com.flightspredictor:flights:0.0.1-SNAPSHOT`
  - No se pudo resolver `org.springframework.boot:spring-boot-starter-parent:pom:3.5.9`
  - Respuesta de Maven Central: `403 Forbidden`
- **Causa raíz resumida:** dependencia padre de Maven inaccesible desde el entorno actual; el proyecto no puede construirse y por eso no inicia la ejecución de tests.
- **Clasificación:** **a) wiring/context** (fallo de contexto de build/arranque de entorno de test; no llega a levantar contexto Spring).

---

## Resumen por tipo de falla (a-e)
- **a) wiring/context:** 2 fallas (una por módulo, ambas en resolución de parent POM).
- **b) cambio de paquetes/clases:** sin evidencia (no se alcanzó compilación/tests).
- **c) mocking:** sin evidencia (no se ejecutaron tests).
- **d) cambios de contrato:** sin evidencia (no se ejecutaron tests).
- **e) tiempo/clock/fechas:** sin evidencia (no se ejecutaron tests).

---

## Nota
Con el entorno actual (403 en Maven Central para parent POM), no es posible obtener listado granular de clases/métodos de test fallidos ni stacktraces de runtime de tests, porque la ejecución se detiene en la etapa de construcción del proyecto.
