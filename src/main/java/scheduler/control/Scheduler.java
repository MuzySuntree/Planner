package scheduler.control;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eventbus.EventBus;
import thinking.model.AIResponseEvent;
import thinking.model.Event;
import thinking.model.SchedulerDecisionEvent;
import thinking.model.SchedulerStatusEvent;

import java.util.Locale;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

// 调度器：承接 AI 输出，映射功能操作，回传执行状态
public class Scheduler implements Runnable, Consumer<Event> {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final BlockingQueue<AIResponseEvent> incoming = new LinkedBlockingQueue<>();

    @Override
    public void accept(Event event) {
        if (event.topic() != Event.Topic.AI_RESPONSE_EVENT) {
            return;
        }
        if (event.content() instanceof AIResponseEvent aiResponse) {
            incoming.offer(aiResponse);
        }
    }

    @Override
    public void run() {
        EventBus.subscribe(this);
        while (!Thread.currentThread().isInterrupted()) {
            try {
                AIResponseEvent aiResponse = incoming.take();
                SchedulerDecisionEvent decision = parseDecision(aiResponse);
                EventBus.publish(new Event(Event.Topic.SCHEDULER_DECISION_EVENT, decision));
                SchedulerStatusEvent status = execute(decision);
                EventBus.publish(new Event(Event.Topic.SCHEDULER_STATUS_EVENT, status));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                EventBus.publish(new Event(
                        Event.Topic.SCHEDULER_STATUS_EVENT,
                        new SchedulerStatusEvent("unknown", "unknown", "FAILED", e.getMessage())
                ));
            }
        }
    }

    private SchedulerDecisionEvent parseDecision(AIResponseEvent aiResponse) throws Exception {
        String response = aiResponse.response();
        JsonNode root = MAPPER.readTree(response);
        String action = root.path("action").asText("noop");
        String target = root.path("target").asText("none");
        String value = root.path("value").asText("");
        return new SchedulerDecisionEvent(action, target, value, aiResponse.priority());
    }

    private SchedulerStatusEvent execute(SchedulerDecisionEvent decision) {
        String action = decision.action().toLowerCase(Locale.ROOT);
        return switch (action) {
            case "create_task" -> new SchedulerStatusEvent(action, decision.target(), "SUCCESS", "任务创建成功: " + decision.value());
            case "update_task" -> new SchedulerStatusEvent(action, decision.target(), "SUCCESS", "任务更新成功: " + decision.value());
            case "delete_task" -> new SchedulerStatusEvent(action, decision.target(), "SUCCESS", "任务删除成功");
            case "noop" -> new SchedulerStatusEvent(action, decision.target(), "IGNORED", "无操作");
            default -> new SchedulerStatusEvent(action, decision.target(), "FAILED", "未支持的动作: " + action);
        };
    }
}
