package thinking.model;

public record SchedulerDecisionEvent(String action, String target, String value, int priority) implements EventPayload {
}
