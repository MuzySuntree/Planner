package eventbus.model;

public record Event(Topic topic,EventPayload content) {
    public enum Topic{
        EventAIToScheduler(EventAIToScheduler.class),
        EventSchedulerToAI(EventSchedulerToAI.class),
        EventSchedulerToState(EventSchedulerToState.class),
        EventStateToScheduler(EventStateToScheduler.class),
        EventStateToDevice(EventStateToDevice.class),
        EventDeviceToState(EventDeviceToState.class)
        ;


        public final Class<?> payloadType;
        Topic(Class<?> payloadType) {
            this.payloadType = payloadType;
        }
    }
}
