import eventbus.EventBus;
import scheduler.control.Scheduler;
import thinking.Control.AiScheduler;
import thinking.model.AIEvent;
import thinking.model.Event;
import thinking.model.SchedulerDecisionEvent;
import thinking.model.SchedulerStatusEvent;

public class Main {
    public static void main(String[] args) {
        AiScheduler aiScheduler = new AiScheduler();

        // AI大脑线程
        Thread aiThread = new Thread(aiScheduler::runLoop, "ai-loop");
        // 调度器线程
        Thread schedulerThread = new Thread(new Scheduler(), "scheduler");

        // 观察调度链路输出
        EventBus.subscribe(event -> {
            if (event.content() instanceof SchedulerDecisionEvent decision) {
                System.out.printf("[Decision] action=%s target=%s value=%s priority=%d%n",
                        decision.action(), decision.target(), decision.value(), decision.priority());
            } else if (event.content() instanceof SchedulerStatusEvent status) {
                System.out.printf("[Status] action=%s target=%s status=%s detail=%s%n",
                        status.action(), status.target(), status.status(), status.detail());
            }
        });

        aiThread.start();
        schedulerThread.start();

        // 示例：向 AI 发布任务，让其返回 JSON 决策
        EventBus.publish(new Event(
                Event.Topic.AI_EVENT,
                new AIEvent(
                        10,
                        "你是调度决策助手，只输出 JSON，字段为 action/target/value。",
                        "请创建一个任务：明天上午9点提醒我开周会"
                )
        ));
    }
}
