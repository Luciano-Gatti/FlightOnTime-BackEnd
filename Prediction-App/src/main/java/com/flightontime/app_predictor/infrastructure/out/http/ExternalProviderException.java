package com.flightontime.app_predictor.infrastructure.out.http;

/**
 * Runtime exception for external provider HTTP failures.
 */
public class ExternalProviderException extends RuntimeException {
    private static final int MAX_BODY_LENGTH = 512;

    private final String provider;
    private final int statusCode;
    private final String bodyTruncated;

    public ExternalProviderException(
            String provider,
            int statusCode,
            String message,
            String body,
            Throwable cause
    ) {
        super(message, cause);
        this.provider = provider;
        this.statusCode = statusCode;
        this.bodyTruncated = truncate(body);
    }

    public String getProvider() {
        return provider;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getBodyTruncated() {
        return bodyTruncated;
    }

    private static String truncate(String body) {
        if (body == null) {
            return null;
        }
        if (body.length() <= MAX_BODY_LENGTH) {
            return body;
        }
        return body.substring(0, MAX_BODY_LENGTH) + "...(truncated)";
    }
}

