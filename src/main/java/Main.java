import scheduler.control.Scheduler;
import thinking.Control.AiScheduler;

public class Main {
    public static void main(String[] args) {
        AiScheduler scheduler = new AiScheduler();

//        AI大脑线程
        Thread aiThread = new Thread(scheduler::runLoop, "ai-worker");
//        调度器线程
        Thread schedulerThread = new Thread(new Scheduler(), "scheduler");

        aiThread.start();
        schedulerThread.start();
    }
}
