package eventbus.task.configure;

import java.util.concurrent.atomic.AtomicReference;

//任务生命周期管理
public class EventTaskLifecycle {
//    默认当前状态为PENDING
    private final AtomicReference<EventTaskState> state = new AtomicReference<>(EventTaskState.PENDING);

    public EventTaskState get() {
        return state.get();
    }

    public boolean trySet(EventTaskState next) {
        if (next == null) throw new IllegalArgumentException("当前状态"+state+"已是终态");
        while (true) {
            EventTaskState prev = state.get();
            if (prev == next) return true; // 幂等

            if (!prev.canTransitionTo(next)) {
                throw new IllegalStateException("非法状态跳转: " + prev + " -> " + next);
            }

            if (state.compareAndSet(prev, next)) {
                return true;
            }
        }
    }
}
