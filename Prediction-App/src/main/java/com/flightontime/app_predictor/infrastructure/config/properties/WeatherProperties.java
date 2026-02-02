package com.flightontime.app_predictor.infrastructure.config.properties;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "weather")
public class WeatherProperties {
    private Service service = new Service();
    private Openmeteo openmeteo = new Openmeteo();
    private Fallback fallback = new Fallback();
    private Cache cache = new Cache();

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public Openmeteo getOpenmeteo() {
        return openmeteo;
    }

    public void setOpenmeteo(Openmeteo openmeteo) {
        this.openmeteo = openmeteo;
    }

    public Fallback getFallback() {
        return fallback;
    }

    public void setFallback(Fallback fallback) {
        this.fallback = fallback;
    }

    public Cache getCache() {
        return cache;
    }

    public void setCache(Cache cache) {
        this.cache = cache;
    }

    public static class Service {
        private String baseUrl;
        private Duration timeoutConnect;
        private Duration timeoutRead;

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public Duration getTimeoutConnect() {
            return timeoutConnect;
        }

        public void setTimeoutConnect(Duration timeoutConnect) {
            this.timeoutConnect = timeoutConnect;
        }

        public Duration getTimeoutRead() {
            return timeoutRead;
        }

        public void setTimeoutRead(Duration timeoutRead) {
            this.timeoutRead = timeoutRead;
        }
    }

    public static class Openmeteo {
        private String baseUrl;
        private String geocodingBaseUrl;

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getGeocodingBaseUrl() {
            return geocodingBaseUrl;
        }

        public void setGeocodingBaseUrl(String geocodingBaseUrl) {
            this.geocodingBaseUrl = geocodingBaseUrl;
        }
    }

    public static class Fallback {
        private String baseUrl;
        private String apiKey;
        private Duration timeoutConnect;
        private Duration timeoutRead;

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public Duration getTimeoutConnect() {
            return timeoutConnect;
        }

        public void setTimeoutConnect(Duration timeoutConnect) {
            this.timeoutConnect = timeoutConnect;
        }

        public Duration getTimeoutRead() {
            return timeoutRead;
        }

        public void setTimeoutRead(Duration timeoutRead) {
            this.timeoutRead = timeoutRead;
        }
    }

    public static class Cache {
        private Duration ttl;

        public Duration getTtl() {
            return ttl;
        }

        public void setTtl(Duration ttl) {
            this.ttl = ttl;
        }
    }
}
