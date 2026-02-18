package thinking.model;

public record Event(Topic topic,EventPayload content) {
    public enum Topic{
        AIEvent(AIEvent.class);

        public final Class<?> payloadType;
        Topic(Class<?> payloadType) {
            this.payloadType = payloadType;
        }
    }
}
