package Control;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

//AI任务调度器
public class AiScheduler {
    private final PriorityBlockingQueue<ScheduledTask> queue = new PriorityBlockingQueue<>();
    private final AtomicReference<ScheduledTask> running = new AtomicReference<>(null);
//    事件加入队列
    public void submit(ScheduledTask t) {
        System.out.println("队列新增任务");
        queue.put(t);

        // 抢占逻辑：如果新任务优先级更高，中断当前任务
        ScheduledTask cur = running.get();
        if (cur != null && t.priority > cur.priority) {
            cur.task.interrupt();
        }
    }
//    队列按优先级执行
    public void runLoop() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                ScheduledTask next = queue.take(); // 阻塞获取最高优先级
                running.set(next);

                boolean finished = next.task.getAnswer();

                if (!finished) {
                    // 被中断：重新入队等待恢复（优先级保持不变）
                    queue.put(next);
                }else {
                    System.out.println("完成:");
                    System.out.println(next.task.full.toString());
                }

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
