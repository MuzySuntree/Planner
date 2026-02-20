package eventbus.task.configure;

import eventbus.model.Event;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

//所有调度器任务的父类
public class AbstractEventTask implements EventTask {
    private final AtomicReference<EventTaskState> state =
            new AtomicReference<>(EventTaskState.PENDING);

    protected volatile EventTaskCallBack callback;
    protected int priority;

    @Override
    public void runOnce() {
        transitionTo(EventTaskState.RUNNING);
        doRun();
    }
    public void doRun(){
    }

    @Override
    public void requestInterrupt() {
        transitionTo(EventTaskState.INTERRUPT_REQUESTED);
    }

    @Override
    public void cancel() {
        transitionTo(EventTaskState.CANCELED);
    }

    @Override
    public Optional<Event> completionEvent() {
        return Optional.empty();
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public void setEventTaskCallBack(EventTaskCallBack eventTaskCallBack) {
        this.callback = eventTaskCallBack;
    }

    @Override
    public EventTaskState getTaskState() {
        return state.get();
    }

    protected void transitionTo(EventTaskState next) {
        while (true) {
            var prev = state.get();
//            重复改变相同状态不触发回调
            if (prev == next) return;
            if (!prev.canTransitionTo(next)) {
                throw new IllegalStateException("非法状态跳转: " + prev + " -> " + next);
            }
            if (state.compareAndSet(prev, next)) {
//                状态改变触发回调函数
                fireCallback(next);
                return;
            }
        }
    }

    private void fireCallback(EventTaskState s) {
        if (callback == null) return;
        switch (s) {
            case FINISHED -> callback.onFinished(this);
            case FAILED -> callback.onFailed(this, null);
            case CANCELED -> callback.onCancelled(this);
            case INTERRUPT_REQUESTED -> callback.onInterruptRequested(this);
            case INTERRUPTED -> callback.onInterrupt(this);
            default -> { /* INTERRUPT_REQUESTED/INTERRUPTED 通常也要有 */ }
        }
    }
}
