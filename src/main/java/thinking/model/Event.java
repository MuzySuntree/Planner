package thinking.model;

public record Event(Topic topic, EventPayload content) {
    public enum Topic {
        AI_EVENT(AIEvent.class),
        AI_RESPONSE_EVENT(AIResponseEvent.class),
        SCHEDULER_DECISION_EVENT(SchedulerDecisionEvent.class),
        SCHEDULER_STATUS_EVENT(SchedulerStatusEvent.class);

        public final Class<?> payloadType;

        Topic(Class<?> payloadType) {
            this.payloadType = payloadType;
        }
    }
}
