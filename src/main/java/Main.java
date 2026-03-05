import eventbus.task.EventTask_AIScheduled;
import eventbus.task.EventTask_Scheduled;
import scheduler.Scheduler;
import scheduler.AiScheduler;
import eventbus.SchedulerInterface;
import scheduler.StateScheduler;

public class Main {
    public static void main(String[] args) {
        SchedulerInterface aiScheduler = new AiScheduler();
//        AI大脑线程
        Thread aiThread = new Thread(aiScheduler::runLoop, "ai-worker");

        SchedulerInterface scheduler = new Scheduler();
//        调度器线程
        Thread schedulerThread = new Thread(scheduler::runLoop, "scheduler");

//        状态模块线程
        SchedulerInterface stateScheduler = new StateScheduler();
        Thread stateThread = new Thread(stateScheduler::runLoop, "state");

        aiThread.start();
        schedulerThread.start();
        stateThread.start();
    }
}
