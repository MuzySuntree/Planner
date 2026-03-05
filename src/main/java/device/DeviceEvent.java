package device;

import org.jetbrains.annotations.NotNull;
import statecentre.model.Device;

public record DeviceEvent(EventType eventType, Device device) {
    public enum EventType{
        REGISTER
    }

}
