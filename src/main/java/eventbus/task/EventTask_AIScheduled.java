package eventbus.task;

import eventbus.model.Event;
import eventbus.model.EventAIToScheduler;
import eventbus.task.configure.AbstractEventTask;
import eventbus.task.configure.EventTaskState;
import thinking.Control.OllamaTask;
import thinking.model.OllamaResult;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

//AI调度器任务类
public class EventTask_AIScheduled extends AbstractEventTask implements Comparable<EventTask_AIScheduled> {
    private static final AtomicLong SEQ = new AtomicLong(0);

    private final long seq;
    private final OllamaTask task;

    public EventTask_AIScheduled(int priority, OllamaTask task) {
        this.seq = SEQ.incrementAndGet();
        this.priority = priority;
        this.task = task;
    }

    @Override
    public Optional<Event> completionEvent() {
        return (getTaskState()==EventTaskState.FINISHED)
                ? Optional.of(new Event(Event.Topic.EventAIToScheduler, new EventAIToScheduler()))
                : Optional.empty();
    }

    @Override
    public void cancel() {
        requestInterrupt();
        transitionTo(EventTaskState.CANCELED);
    }

    @Override
    public void doRun() {
        try {
            OllamaResult ollamaResult = task.getAnswer();
            if(ollamaResult.interrupted()){
                transitionTo(EventTaskState.INTERRUPTED);
            }else if(ollamaResult.finished()){
                transitionTo(EventTaskState.FINISHED);
            }
        }catch (IOException e){
            e.printStackTrace();
            transitionTo(EventTaskState.FAILED);
        }
    }

    @Override
    public void requestInterrupt() {
        transitionTo(EventTaskState.INTERRUPT_REQUESTED);
        task.interrupt();
    }

    @Override
    public int compareTo(EventTask_AIScheduled o) {
        // priority 大的先执行；priority 相同按 seq 小的先
        int p = Integer.compare(o.priority, this.priority);
        return (p != 0) ? p : Long.compare(this.seq, o.seq);
    }
}
