package eventbus.task;

import thinking.Control.OllamaTask;

import java.util.concurrent.atomic.AtomicLong;

//承载AI调度任务
public class EventTask_AIScheduled implements Comparable<EventTask_AIScheduled> {
    private static final AtomicLong SEQ = new AtomicLong(0);

    public final long seq;
    public final int priority;
    public final OllamaTask task;

    public EventTask_AIScheduled(int priority, OllamaTask task) {
        this.seq = SEQ.incrementAndGet();
        this.priority = priority;
        this.task = task;
    }

    @Override
    public int compareTo(EventTask_AIScheduled o) {
        // priority 大的先执行；priority 相同按 seq 小的先
        int p = Integer.compare(o.priority, this.priority);
        return (p != 0) ? p : Long.compare(this.seq, o.seq);
    }
}
