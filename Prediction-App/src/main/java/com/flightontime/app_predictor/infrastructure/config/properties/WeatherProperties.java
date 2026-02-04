package com.flightontime.app_predictor.infrastructure.config.properties;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Clase WeatherProperties.
 */
@ConfigurationProperties(prefix = "weather")
public class WeatherProperties {
    private Service service = new Service();
    private Openmeteo openmeteo = new Openmeteo();
    private Fallback fallback = new Fallback();
    private Cache cache = new Cache();

    /**
     * Ejecuta la operación get service.
     * @return resultado de la operación get service.
     */

    public Service getService() {
        return service;
    }

    /**
     * Ejecuta la operación set service.
     * @param service variable de entrada service.
     */

    public void setService(Service service) {
        this.service = service;
    }

    /**
     * Ejecuta la operación get openmeteo.
     * @return resultado de la operación get openmeteo.
     */

    public Openmeteo getOpenmeteo() {
        return openmeteo;
    }

    /**
     * Ejecuta la operación set openmeteo.
     * @param openmeteo variable de entrada openmeteo.
     */

    public void setOpenmeteo(Openmeteo openmeteo) {
        this.openmeteo = openmeteo;
    }

    /**
     * Ejecuta la operación get fallback.
     * @return resultado de la operación get fallback.
     */

    public Fallback getFallback() {
        return fallback;
    }

    /**
     * Ejecuta la operación set fallback.
     * @param fallback variable de entrada fallback.
     */

    public void setFallback(Fallback fallback) {
        this.fallback = fallback;
    }

    /**
     * Ejecuta la operación get cache.
     * @return resultado de la operación get cache.
     */

    public Cache getCache() {
        return cache;
    }

    /**
     * Ejecuta la operación set cache.
     * @param cache variable de entrada cache.
     */

    public void setCache(Cache cache) {
        this.cache = cache;
    }

    /**
     * Clase Service.
     */
    public static class Service {
        private String baseUrl;
        private Duration timeoutConnect;
        private Duration timeoutRead;

        /**
         * Ejecuta la operación get base url.
         * @return resultado de la operación get base url.
         */

        public String getBaseUrl() {
            return baseUrl;
        }

        /**
         * Ejecuta la operación set base url.
         * @param baseUrl variable de entrada baseUrl.
         */

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        /**
         * Ejecuta la operación get timeout connect.
         * @return resultado de la operación get timeout connect.
         */

        public Duration getTimeoutConnect() {
            return timeoutConnect;
        }

        /**
         * Ejecuta la operación set timeout connect.
         * @param timeoutConnect variable de entrada timeoutConnect.
         */

        public void setTimeoutConnect(Duration timeoutConnect) {
            this.timeoutConnect = timeoutConnect;
        }

        /**
         * Ejecuta la operación get timeout read.
         * @return resultado de la operación get timeout read.
         */

        public Duration getTimeoutRead() {
            return timeoutRead;
        }

        /**
         * Ejecuta la operación set timeout read.
         * @param timeoutRead variable de entrada timeoutRead.
         */

        public void setTimeoutRead(Duration timeoutRead) {
            this.timeoutRead = timeoutRead;
        }
    }

    /**
     * Clase Openmeteo.
     */
    public static class Openmeteo {
        private String baseUrl;
        private String geocodingBaseUrl;

        /**
         * Ejecuta la operación get base url.
         * @return resultado de la operación get base url.
         */

        public String getBaseUrl() {
            return baseUrl;
        }

        /**
         * Ejecuta la operación set base url.
         * @param baseUrl variable de entrada baseUrl.
         */

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        /**
         * Ejecuta la operación get geocoding base url.
         * @return resultado de la operación get geocoding base url.
         */

        public String getGeocodingBaseUrl() {
            return geocodingBaseUrl;
        }

        /**
         * Ejecuta la operación set geocoding base url.
         * @param geocodingBaseUrl variable de entrada geocodingBaseUrl.
         */

        public void setGeocodingBaseUrl(String geocodingBaseUrl) {
            this.geocodingBaseUrl = geocodingBaseUrl;
        }
    }

    /**
     * Clase Fallback.
     */
    public static class Fallback {
        private String baseUrl;
        private String apiKey;
        private Duration timeoutConnect;
        private Duration timeoutRead;

        /**
         * Ejecuta la operación get base url.
         * @return resultado de la operación get base url.
         */

        public String getBaseUrl() {
            return baseUrl;
        }

        /**
         * Ejecuta la operación set base url.
         * @param baseUrl variable de entrada baseUrl.
         */

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        /**
         * Ejecuta la operación get api key.
         * @return resultado de la operación get api key.
         */

        public String getApiKey() {
            return apiKey;
        }

        /**
         * Ejecuta la operación set api key.
         * @param apiKey variable de entrada apiKey.
         */

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        /**
         * Ejecuta la operación get timeout connect.
         * @return resultado de la operación get timeout connect.
         */

        public Duration getTimeoutConnect() {
            return timeoutConnect;
        }

        /**
         * Ejecuta la operación set timeout connect.
         * @param timeoutConnect variable de entrada timeoutConnect.
         */

        public void setTimeoutConnect(Duration timeoutConnect) {
            this.timeoutConnect = timeoutConnect;
        }

        /**
         * Ejecuta la operación get timeout read.
         * @return resultado de la operación get timeout read.
         */

        public Duration getTimeoutRead() {
            return timeoutRead;
        }

        /**
         * Ejecuta la operación set timeout read.
         * @param timeoutRead variable de entrada timeoutRead.
         */

        public void setTimeoutRead(Duration timeoutRead) {
            this.timeoutRead = timeoutRead;
        }
    }

    /**
     * Clase Cache.
     */
    public static class Cache {
        private Duration ttl;

        /**
         * Ejecuta la operación get ttl.
         * @return resultado de la operación get ttl.
         */

        public Duration getTtl() {
            return ttl;
        }

        /**
         * Ejecuta la operación set ttl.
         * @param ttl variable de entrada ttl.
         */

        public void setTtl(Duration ttl) {
            this.ttl = ttl;
        }
    }
}
