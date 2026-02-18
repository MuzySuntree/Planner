package thinking.Control;

import thinking.model.AIEvent;
import thinking.model.Event;
import thinking.model.OllamaChatRequest;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

public class AICommWorker implements Runnable, Consumer<Event> {

    private final AiScheduler scheduler;
    private final BlockingQueue<AIEvent> incomingEvents = new LinkedBlockingQueue<>();

    public AICommWorker(AiScheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public void accept(Event event) {
        if (event.topic() != Event.Topic.AI_EVENT) return;
        if (!(event.content() instanceof AIEvent aiEvent)) return;
        incomingEvents.offer(aiEvent);
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                AIEvent aiEvent = incomingEvents.take();
                var chat = new OllamaChatRequest(aiEvent.systemPrompt(), aiEvent.userPrompt());
                var task = new OllamaTask(chat);
                scheduler.submit(new ScheduledTask(aiEvent.priority(), task));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
