package eventbus.commWorker;

import eventbus.SchedulerInterface;
import eventbus.model.EventSchedulerToAI;
import scheduler.AiScheduler;
import thinking.Control.OllamaTask;
import eventbus.task.EventTask_AIScheduled;
import eventbus.model.Event;
import thinking.model.OllamaChatRequest;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

//AI订阅调度器
public class CommWorkerAISubscribeScheduler implements Runnable, Consumer<Event> {

    private final SchedulerInterface scheduler;
    private final BlockingQueue<EventSchedulerToAI> incomingEvents = new LinkedBlockingQueue<>();

    public CommWorkerAISubscribeScheduler(AiScheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public void accept(Event event) {
        if(!(event.content() instanceof EventSchedulerToAI aiEvent)) return;
        incomingEvents.offer(aiEvent);
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                EventSchedulerToAI aiEvent = incomingEvents.take();
                var chat = new OllamaChatRequest(aiEvent.systemPrompt(), aiEvent.userPrompt());
                var task = new OllamaTask(chat);
                scheduler.submit(new EventTask_AIScheduled(aiEvent.priority(), task));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
