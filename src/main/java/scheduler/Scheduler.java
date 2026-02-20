package scheduler;


import eventbus.EventBus;
import eventbus.SchedulerInterface;
import eventbus.commWorker.CommWorkerSchedulerSubscribeAI;
import eventbus.commWorker.CommWorkerSchedulerSubscribeState;
import eventbus.task.configure.EventTask;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

//调度器
public class Scheduler implements SchedulerInterface {

    private final PriorityBlockingQueue<EventTask> queue = new PriorityBlockingQueue<>();
    private final AtomicReference<EventTask> running = new AtomicReference<>(null);

    CommWorkerSchedulerSubscribeAI commWorker_AI = new CommWorkerSchedulerSubscribeAI(this);
    CommWorkerSchedulerSubscribeState commWorker_State = new CommWorkerSchedulerSubscribeState(this);

    public Scheduler() {
//        订阅AI的回复
        EventBus.subscribe(commWorker_AI);
//        订阅状态模块的回复
        EventBus.subscribe(commWorker_State);

        new Thread(commWorker_AI,"commWorker_AI").start();
        new Thread(commWorker_State,"commWorker_State").start();
    }

    @Override
    public void runLoop() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                EventTask next = queue.take(); // 阻塞获取最高优先级
                running.set(next);

                next.run();

                running.set(null);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                // 任务执行异常：你可以决定丢弃/重试/标记失败
                e.printStackTrace();
                running.set(null);
            }
        }
    }

    @Override
    public void submit(EventTask t) {
        queue.put(t);
        // 抢占逻辑：如果新任务优先级更高，中断当前任务
        EventTask cur = running.get();
        if (cur != null && t.getPriority() > cur.getPriority()) {
            cur.interrupt();
        }
    }
}
