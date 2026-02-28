package eventbus.commWorker;

import eventbus.SchedulerInterface;
import eventbus.model.Event;
import eventbus.model.EventSchedulerToState;
import eventbus.task.EventTask_State;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

//状态模块订阅调度器
public class CommWorkerStateSubscribeScheduler implements Runnable, Consumer<Event> {
    SchedulerInterface scheduler;

    private final BlockingQueue<EventSchedulerToState> incomingEvents = new LinkedBlockingQueue<>();

    public CommWorkerStateSubscribeScheduler(SchedulerInterface scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                EventSchedulerToState aiEvent = incomingEvents.take();

                scheduler.submit(new EventTask_State());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public void accept(Event event) {
        if(event.content() instanceof EventSchedulerToState eventSchedulerToState) {
            incomingEvents.offer(eventSchedulerToState);
        }
    }
}
