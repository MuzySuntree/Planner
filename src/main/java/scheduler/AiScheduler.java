package scheduler;

import eventbus.EventBus;
import eventbus.SchedulerInterface;
import eventbus.commWorker.CommWorkerAISubscribeScheduler;
import eventbus.task.configure.EventTask;
import eventbus.task.configure.EventTaskCallBack;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

//AI任务调度器
public class AiScheduler implements SchedulerInterface {
    private final PriorityBlockingQueue<EventTask> queue = new PriorityBlockingQueue<>();
    private final AtomicReference<EventTask> running = new AtomicReference<>(null);

//    负责订阅事件
    CommWorkerAISubscribeScheduler commWorker = new CommWorkerAISubscribeScheduler(this);

    public AiScheduler() {
        EventBus.subscribe(commWorker);
        new Thread(commWorker, "ai-worker").start();
    }

//    事件加入队列
    public void submit(EventTask t) {
        t.setEventTaskCallBack(new EventTaskCallBack() {

            @Override
            public void onFinished(EventTask eventTask) {
                //发布事件到调度器
                eventTask.completionEvent().ifPresent(EventBus::publish);
            }

            @Override
            public void onFailed(EventTask eventTask, Throwable throwable) {

            }

            @Override
            public void onCancelled(EventTask eventTask) {

            }

            @Override
            public void onInterrupt(EventTask eventTask) {
                // 被中断：重新入队等待恢复（优先级保持不变）
                queue.put(eventTask);
            }

            @Override
            public void onInterruptRequested(EventTask eventTask) {

            }
        });

        queue.put(t);
        // 抢占逻辑：如果新任务优先级更高，中断当前任务
        EventTask cur = running.get();
        if (cur != null && t.getPriority() > cur.getPriority()) {
            cur.requestInterrupt();
        }
    }
//    队列按优先级执行
    public void runLoop() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                EventTask next = queue.take(); // 阻塞获取最高优先级
                running.set(next);
                next.runOnce();
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
}
