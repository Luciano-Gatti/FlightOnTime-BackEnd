package com.flightontime.app_predictor.infrastructure.out.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;

public class FlexibleOffsetDateTimeDeserializer extends JsonDeserializer<OffsetDateTime> {
    private static final String[] DATE_FIELDS = {
            "utc",
            "actualDeparture",
            "actualArrival",
            "departure",
            "arrival"
    };

    @Override
    public OffsetDateTime deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        JsonNode node = parser.getCodec().readTree(parser);
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isTextual()) {
            return parse(node.asText());
        }
        if (node.isObject()) {
            for (String field : DATE_FIELDS) {
                JsonNode value = node.get(field);
                if (value != null && value.isTextual()) {
                    OffsetDateTime parsed = parse(value.asText());
                    if (parsed != null) {
                        return parsed;
                    }
                }
            }
        }
        return null;
    }

    private OffsetDateTime parse(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return OffsetDateTime.parse(value);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }
}
