package eventbus.model;

public record EventStateToDevice(OperationType operationType, Object payload) implements EventPayload {
    public enum OperationType {
        Requestion,
        Response,
    }
}
