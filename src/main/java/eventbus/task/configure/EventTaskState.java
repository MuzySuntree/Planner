package eventbus.task.configure;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

//任务的生命周期状态
public enum EventTaskState {
    PENDING,             //任务处于就绪状态
    RUNNING,             //任务正在运行
    INTERRUPT_REQUESTED, // 仅表示请求中断
    INTERRUPTED,         // 已确认中断并停止
    FINISHED,            //任务已完成
    FAILED,             //任务失败
    CANCELED;            //任务已取消

//    有限自动机状态机
    private static final Map<EventTaskState, Set<EventTaskState>> ALLOWED = new EnumMap<>(EventTaskState.class);

    static {
        ALLOWED.put(PENDING, EnumSet.of(EventTaskState.RUNNING,EventTaskState.CANCELED));
        ALLOWED.put(RUNNING, EnumSet.of(EventTaskState.INTERRUPT_REQUESTED, EventTaskState.FINISHED, EventTaskState.FAILED, EventTaskState.CANCELED));
        ALLOWED.put(INTERRUPT_REQUESTED, EnumSet.of(EventTaskState.INTERRUPTED, EventTaskState.FINISHED, EventTaskState.FAILED, EventTaskState.CANCELED));
        ALLOWED.put(INTERRUPTED, EnumSet.of(EventTaskState.CANCELED, EventTaskState.RUNNING));
        ALLOWED.put(FINISHED, EnumSet.noneOf(EventTaskState.class));
        ALLOWED.put(FAILED, EnumSet.of(EventTaskState.PENDING));
        ALLOWED.put(CANCELED, EnumSet.noneOf(EventTaskState.class));
    }

    public boolean canTransitionTo(EventTaskState next) {
        return ALLOWED.getOrDefault(this, EnumSet.noneOf(EventTaskState.class)).contains(next);
    }
}
