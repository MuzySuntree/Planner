package eventbus;

import eventbus.task.configure.EventTask;

public interface SchedulerInterface {
    void runLoop();
    void submit(EventTask t);
}
