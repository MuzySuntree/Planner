package eventbus.commWorker;

import eventbus.SchedulerInterface;
import eventbus.model.Event;
import eventbus.model.EventAIToScheduler;
import eventbus.model.EventStateToScheduler;
import eventbus.task.EventTask_Scheduled;
import scheduler.Scheduler;
import thinking.Control.OllamaTask;
import thinking.model.OllamaChatRequest;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

//调度器订阅状态
public class CommWorkerSchedulerSubscribeState implements Runnable, Consumer<Event> {
    private final BlockingQueue<EventStateToScheduler> incomingEvents = new LinkedBlockingQueue<>();
    private final SchedulerInterface scheduler;

    public CommWorkerSchedulerSubscribeState(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                EventStateToScheduler eventAIToScheduler = incomingEvents.take();

                scheduler.submit(new EventTask_Scheduled(EventTask_Scheduled.EventType.Event_State));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public void accept(Event event) {
        if(!(event.content() instanceof EventStateToScheduler aiEvent)) return;
        incomingEvents.offer(aiEvent);
    }
}
