package thinking.model;

public record AIEvent(int priority, String systemPrompt, String userPrompt) implements EventPayload {
}
