package eventbus.model;

public record EventSchedulerToAI(int priority, String systemPrompt, String userPrompt) implements EventPayload {
}
