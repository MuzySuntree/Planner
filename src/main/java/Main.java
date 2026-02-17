import Control.AiScheduler;
import Control.CommWorker;
import Control.SchedulerEventBus;
import Control.SchedulerEventProducer;

public class Main {
    public static void main(String[] args) {
        AiScheduler scheduler = new AiScheduler();
        SchedulerEventBus eventBus = new SchedulerEventBus();

        CommWorker commWorker = new CommWorker(scheduler);
        eventBus.subscribe(commWorker);

        Thread aiThread = new Thread(scheduler::runLoop, "ai-worker");
        Thread commThread = new Thread(commWorker, "comm-worker");
        Thread schedulerEventThread = new Thread(new SchedulerEventProducer(eventBus), "scheduler-event-producer");

        aiThread.start();
        commThread.start();
        schedulerEventThread.start();
    }
}
