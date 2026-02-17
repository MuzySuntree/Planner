package Control;

import model.OllamaChatRequest;
import model.SchedulerEvent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

public class CommWorker implements Runnable, Consumer<SchedulerEvent> {

    private final AiScheduler scheduler;
    private final BlockingQueue<SchedulerEvent> incomingEvents = new LinkedBlockingQueue<>();

    public CommWorker(AiScheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public void accept(SchedulerEvent event) {
        incomingEvents.offer(event);
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                SchedulerEvent event = incomingEvents.take();
                var chat = new OllamaChatRequest(event.getSystemPrompt(), event.getUserPrompt());
                var task = new OllamaTask(chat);
                scheduler.submit(new ScheduledTask(event.getPriority(), task));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
