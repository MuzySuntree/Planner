package scheduler;


import eventbus.SchedulerInterface;
import eventbus.worker.CommWorkerSchedulerSubscribeAI;
import eventbus.task.EventTask_Scheduled;

//调度器
public class Scheduler implements SchedulerInterface<EventTask_Scheduled> {

    CommWorkerSchedulerSubscribeAI commWorker = new CommWorkerSchedulerSubscribeAI(this);

    public Scheduler() {
    }

    @Override
    public void runLoop() {

    }

    @Override
    public void submit(EventTask_Scheduled t) {

    }
}
