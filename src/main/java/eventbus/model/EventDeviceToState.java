package eventbus.model;

import statecentre.model.Device;

public record EventDeviceToState(String id, Device device, DeviceCmd deviceCmd) implements EventPayload {
    public enum DeviceCmd{
        Register,
        Unregister,
        Change,
    }
}
