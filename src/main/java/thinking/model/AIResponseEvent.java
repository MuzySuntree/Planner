package thinking.model;

public record AIResponseEvent(int priority, String response) implements EventPayload {
}
