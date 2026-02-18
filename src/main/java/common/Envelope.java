package common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Envelope(
        String eventType,
        long timestamp,
        String correlationId,
        String commandId,
        String source,
        String target,
        JsonNode payload,
        ErrorInfo error
) {
}
