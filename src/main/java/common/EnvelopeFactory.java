package common;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.UUID;

public final class EnvelopeFactory {
    private EnvelopeFactory() {}

    public static Envelope of(String eventType, String source, String target, String correlationId, String commandId, Object payload) {
        JsonNode payloadNode = Jsons.MAPPER.valueToTree(payload);
        return new Envelope(eventType, System.currentTimeMillis(), correlationId, commandId, source, target, payloadNode, null);
    }

    public static String newCorrelationId() {
        return "corr-" + UUID.randomUUID();
    }

    public static String newCommandId() {
        return "cmd-" + UUID.randomUUID();
    }
}
