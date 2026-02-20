package eventbus.commWorker;

import eventbus.SchedulerInterface;
import eventbus.model.Event;
import eventbus.model.EventAIToScheduler;
import eventbus.model.EventStateToScheduler;
import scheduler.Scheduler;
import eventbus.task.EventTask_Scheduled;
import thinking.Control.OllamaTask;
import thinking.model.OllamaChatRequest;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

//调度器订阅AI
public class CommWorkerSchedulerSubscribeAI implements Runnable, Consumer<Event> {
    private final BlockingQueue<EventAIToScheduler> incomingEvents = new LinkedBlockingQueue<>();
    private final SchedulerInterface scheduler;

    public CommWorkerSchedulerSubscribeAI(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                EventAIToScheduler eventAIToScheduler = incomingEvents.take();

                scheduler.submit(new EventTask_Scheduled(EventTask_Scheduled.EventType.Event_AI));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public void accept(Event event) {
        if(!(event.content() instanceof EventAIToScheduler eventAIToScheduler)) return;
        incomingEvents.offer(eventAIToScheduler);
    }
}
