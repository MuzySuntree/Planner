import eventbus.task.EventTask_AIScheduled;
import eventbus.task.EventTask_Scheduled;
import scheduler.Scheduler;
import scheduler.AiScheduler;
import eventbus.SchedulerInterface;

public class Main {
    public static void main(String[] args) {
        SchedulerInterface aiScheduler = new AiScheduler();
//        AI大脑线程
        Thread aiThread = new Thread(aiScheduler::runLoop, "ai-worker");

        SchedulerInterface scheduler = new Scheduler();
//        调度器线程
        Thread schedulerThread = new Thread(scheduler::runLoop, "scheduler");

        aiThread.start();
        schedulerThread.start();
    }
}
