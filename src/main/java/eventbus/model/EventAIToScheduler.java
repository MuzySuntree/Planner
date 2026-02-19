package eventbus.model;

public record EventAIToScheduler(int priority, String systemPrompt, String userPrompt) implements EventPayload {
}
