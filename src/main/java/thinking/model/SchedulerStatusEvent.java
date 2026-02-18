package thinking.model;

public record SchedulerStatusEvent(String action, String target, String status, String detail) implements EventPayload {
}
