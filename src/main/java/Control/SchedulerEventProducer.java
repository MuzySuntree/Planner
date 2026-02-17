package Control;

import model.SchedulerEvent;

public class SchedulerEventProducer implements Runnable {
    private final SchedulerEventBus eventBus;

    public SchedulerEventProducer(SchedulerEventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(500);
            eventBus.publish(new SchedulerEvent(5, "你是一个只输出JSON的决策助手", "温度过高，生成详细决策JSON，事件>=10条"));

            Thread.sleep(6500);
            eventBus.publish(new SchedulerEvent(10, "你是一个只输出JSON的决策助手", "门铃响了，给出简短决策JSON"));

            Thread.sleep(10000);
            eventBus.publish(new SchedulerEvent(10, "你是一个只输出JSON的决策助手", "门铃响了，给出简短决策JSON"));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
