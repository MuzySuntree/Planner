package bootstrap;

import eventbus.EventBus;
import scheduler.control.Scheduler;
import thinking.Control.AiScheduler;
import thinking.model.AIEvent;
import thinking.model.Event;
import thinking.model.SchedulerDecisionEvent;
import thinking.model.SchedulerStatusEvent;

/**
 * Core 启动入口：不再使用 brain 包，直接使用 thinking.Control 作为 AI 连接模块。
 * 只需要向 EventBus 发布 AI_EVENT，即可收到 AI_RESPONSE 并由 Scheduler 处理为决策与状态事件。
 */
public class CoreMain {
    public static void main(String[] args) {
        AiScheduler aiScheduler = new AiScheduler();

        Thread aiThread = new Thread(aiScheduler::runLoop, "ai-loop");
        Thread schedulerThread = new Thread(new Scheduler(), "scheduler");

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

        // 示例：只需发布 AI_EVENT
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
