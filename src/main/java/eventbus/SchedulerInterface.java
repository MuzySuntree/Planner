package eventbus;

public interface SchedulerInterface<T> {
    void runLoop();
    void submit(T t);
}
