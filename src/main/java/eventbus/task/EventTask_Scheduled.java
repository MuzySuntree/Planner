package eventbus.task;

import eventbus.task.configure.AbstractEventTask;
import eventbus.task.configure.EventTask;
import eventbus.task.configure.EventTaskCallBack;
import eventbus.task.configure.EventTaskState;

public class EventTask_Scheduled extends AbstractEventTask {

    public enum EventType{
        Event_AI,
        Event_State
    }
    final EventType eventType;
    int priority;

    public EventTask_Scheduled(EventType eventType) {
        this.eventType = eventType;
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
    public EventTaskState getTaskState() {
        return null;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }
}
