package Control;

import model.SchedulerEvent;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class SchedulerEventBus {
    private final List<Consumer<SchedulerEvent>> subscribers = new CopyOnWriteArrayList<>();

    public void subscribe(Consumer<SchedulerEvent> subscriber) {
        subscribers.add(subscriber);
    }

    public void publish(SchedulerEvent event) {
        for (Consumer<SchedulerEvent> subscriber : subscribers) {
            subscriber.accept(event);
        }
    }
}
