package eventbus.task;

import device.DeviceEvent;
import eventbus.model.Event;
import eventbus.task.configure.AbstractEventTask;
import eventbus.task.configure.EventTask;
import eventbus.task.configure.EventTaskCallBack;
import eventbus.task.configure.EventTaskState;
import plan.Plan;
import statecentre.model.Device;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class EventTask_Scheduled extends AbstractEventTask {

    public enum EventType{
        Event_AI,
        Event_State
    }
    final EventType eventType;
    int priority;
    Object payload;
    AtomicBoolean interrupted;

    public EventTask_Scheduled(EventType eventType, Object payload) {
        this.eventType = eventType;
        this.payload = payload;
    }

    public EventType getEventType() {
        return eventType;
    }

    public int getPriority() {
        return priority;
    }

    @Override
    public void setEventTaskCallBack(EventTaskCallBack eventTaskCallBack) {

    }

    @Override
    public void runOnce() {
        transitionTo(EventTaskState.RUNNING);
        if(eventType == EventType.Event_AI){
            handleEventAi();
        }else if(eventType == EventType.Event_State){
            handleEventState();
        }
        transitionTo(EventTaskState.FINISHED);
    }

    private void handleEventAi(){
        Plan plan = (Plan) payload;
        System.out.println(plan);
    }

    @Override
    public void requestInterrupt() {
        interrupted.set(true);
        transitionTo(EventTaskState.INTERRUPT_REQUESTED);
    }

    private void handleEventState(){
        DeviceEvent deviceEvent = (DeviceEvent) payload;
        Device device = deviceEvent.getDevice();

    }

    @Override
    public Optional<Event> completionEvent() {
        if(eventType == EventType.Event_AI){
            return Optional.empty();
        }else if(eventType == EventType.Event_State){
            return Optional.of();
        }
        return Optional.empty();
    }

    @Override
    public EventTaskState getTaskState() {
        return null;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }
}
