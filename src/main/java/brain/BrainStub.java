package brain;

import common.Envelope;
import common.EnvelopeFactory;
import common.EventTypes;
import common.Payloads;
import eventbus.InMemoryEventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Brain MVP：规则式计划器，可替换为真实 LLM。
 */
public class BrainStub {
    private final InMemoryEventBus eventBus;
    private final AtomicBoolean stopFlag;

    public BrainStub(InMemoryEventBus eventBus, AtomicBoolean stopFlag) {
        this.eventBus = eventBus;
        this.stopFlag = stopFlag;
    }

    public void start() {
        eventBus.subscribe(EventTypes.BRAIN_USER_COMMAND, this::handleUserCommand);
    }

    private void handleUserCommand(Envelope envelope) {
        Payloads.UserCommand userCommand = parse(envelope, Payloads.UserCommand.class);
        String text = userCommand.text();

        List<Payloads.PlanStep> steps = new ArrayList<>();
        String output;
        if (text.startsWith("echo ")) {
            String body = text.substring(5);
            steps.add(new Payloads.PlanStep(0, "earmouth-console", "console.print", Map.of("text", body)));
            output = "执行 echo";
        } else if ("help".equalsIgnoreCase(text)) {
            steps.add(new Payloads.PlanStep(0, "earmouth-console", "console.print", Map.of("text", "可用命令: help | echo <text> | exit")));
            output = "输出帮助信息";
        } else if ("exit".equalsIgnoreCase(text)) {
            steps.add(new Payloads.PlanStep(0, "earmouth-console", "console.print", Map.of("text", "程序即将退出")));
            output = "退出程序";
            stopFlag.set(true);
        } else {
            steps.add(new Payloads.PlanStep(0, "earmouth-console", "console.print", Map.of("text", "未知命令，输入 help 查看帮助")));
            output = "未知命令提示";
        }

        Payloads.PlanSubmitted plan = new Payloads.PlanSubmitted("plan-" + System.nanoTime(), steps);
        eventBus.publish(EnvelopeFactory.of(
                EventTypes.BRAIN_PLAN_SUBMITTED,
                "brain",
                "scheduler",
                envelope.correlationId(),
                null,
                plan
        ));
        eventBus.publish(EnvelopeFactory.of(
                EventTypes.SCHEDULER_OUTPUT,
                "brain",
                "earmouth-console",
                envelope.correlationId(),
                null,
                new Payloads.SchedulerOutput("[Brain] " + output)
        ));
    }

    private <T> T parse(Envelope envelope, Class<T> clazz) {
        return common.Jsons.MAPPER.convertValue(envelope.payload(), clazz);
    }
}
