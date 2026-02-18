package bootstrap;

import eventbus.EventBus;
import eventbus.InMemoryEventBus;
import scheduler.control.Scheduler;
import statehub.StateHub;
import thinking.Control.AiScheduler;
import thinking.model.AIEvent;
import thinking.model.Event;
import thinking.model.SchedulerDecisionEvent;
import thinking.model.SchedulerStatusEvent;

/**
 * Core 启动入口：
 * 1) 启动 AI 链路（thinking.Control + scheduler.control）
 * 2) 启动 StateHub(Netty:9000)，供 ConsoleOrganMain 连接
 */
public class CoreMain {
    public static void main(String[] args) throws Exception {
        // 启动 StateHub，避免 ConsoleOrganMain 连接 9000 被拒绝
        InMemoryEventBus stateHubBus = new InMemoryEventBus();
        StateHub stateHub = new StateHub(stateHubBus, 9000);
        stateHub.start();

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

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            stateHub.stop();
            stateHubBus.close();
        }));

        // 示例：只需发布 AI_EVENT
        EventBus.publish(new Event(
                Event.Topic.AI_EVENT,
                new AIEvent(
                        10,
                        "你是调度决策助手，只输出 JSON，字段为 action/target/value。",
                        "请创建一个任务：明天上午9点提醒我开周会"
                )
        ));

        // 保持主线程存活，便于外部 ConsoleOrganMain 随后接入
        Thread.currentThread().join();
    }
}
