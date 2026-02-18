package eventbus;

import common.Envelope;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * 简单 in-memory EventBus：单队列入站 + 多消费者分发。
 */
public class InMemoryEventBus implements AutoCloseable {
    private final BlockingQueue<Envelope> queue = new LinkedBlockingQueue<>();
    private final Map<String, CopyOnWriteArrayList<Consumer<Envelope>>> subscribers = new ConcurrentHashMap<>();
    private final ExecutorService dispatcher = Executors.newSingleThreadExecutor(r -> new Thread(r, "eventbus-dispatcher"));
    private final AtomicBoolean running = new AtomicBoolean(true);

    public InMemoryEventBus() {
        dispatcher.submit(this::dispatchLoop);
    }

    public void publish(Envelope envelope) {
        queue.offer(envelope);
    }

    public void subscribe(String eventType, Consumer<Envelope> consumer) {
        subscribers.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(consumer);
    }

    private void dispatchLoop() {
        while (running.get() && !Thread.currentThread().isInterrupted()) {
            try {
                Envelope envelope = queue.take();
                List<Consumer<Envelope>> consumers = subscribers.get(envelope.eventType());
                if (consumers == null) {
                    continue;
                }
                for (Consumer<Envelope> consumer : consumers) {
                    consumer.accept(envelope);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void close() {
        running.set(false);
        dispatcher.shutdownNow();
    }
}
