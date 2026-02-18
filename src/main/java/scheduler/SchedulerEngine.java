package scheduler;

import common.Envelope;
import common.EnvelopeFactory;
import common.EventTypes;
import common.Payloads;
import eventbus.InMemoryEventBus;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 调度器：执行 Brain 提交计划，跟踪步骤状态与 command 生命周期。
 */
public class SchedulerEngine {
    private final InMemoryEventBus eventBus;
    private final Map<String, StepRuntime> waiting = new ConcurrentHashMap<>();

    public SchedulerEngine(InMemoryEventBus eventBus) {
        this.eventBus = eventBus;
    }

    public void start() {
        eventBus.subscribe(EventTypes.BRAIN_PLAN_SUBMITTED, this::handlePlan);
        eventBus.subscribe(EventTypes.SCHEDULER_COMMAND_RESULT, this::handleCommandResult);
        eventBus.subscribe(EventTypes.SCHEDULER_COMMAND_TIMEOUT, this::handleCommandTimeout);
    }

    private void handlePlan(Envelope envelope) {
        Payloads.PlanSubmitted plan = parse(envelope, Payloads.PlanSubmitted.class);
        for (Payloads.PlanStep step : plan.steps()) {
            String commandId = EnvelopeFactory.newCommandId();
            waiting.put(commandId, new StepRuntime(plan.planId(), step.order(), "waiting", envelope.correlationId()));

            Payloads.OrganCommand cmd = new Payloads.OrganCommand(step.organId(), step.capabilityId(), step.args());
            log("submit", commandId, envelope.correlationId(), step.organId(), step.capabilityId());
            eventBus.publish(EnvelopeFactory.of(
                    EventTypes.SCHEDULER_COMMAND,
                    "scheduler",
                    "statehub",
                    envelope.correlationId(),
                    commandId,
                    cmd
            ));
        }
    }

    private void handleCommandResult(Envelope envelope) {
        StepRuntime runtime = waiting.remove(envelope.commandId());
        Payloads.OrganResult result = parse(envelope, Payloads.OrganResult.class);
        if (runtime == null) {
            log("late-result", envelope.commandId(), envelope.correlationId(), result.organId(), result.capabilityId());
            return;
        }
        runtime.status = result.success() ? "success" : "fail";
        log("result-" + runtime.status, envelope.commandId(), envelope.correlationId(), result.organId(), result.capabilityId());
    }

    private void handleCommandTimeout(Envelope envelope) {
        StepRuntime runtime = waiting.remove(envelope.commandId());
        if (runtime == null) {
            return;
        }
        runtime.status = "timeout";
        log("timeout", envelope.commandId(), envelope.correlationId(), "unknown", "unknown");
    }

    private void log(String phase, String commandId, String corr, String organId, String capabilityId) {
        System.out.printf("[Scheduler] %s commandId=%s correlationId=%s organ=%s capability=%s%n",
                phase, commandId, corr, organId, capabilityId);
    }

    private <T> T parse(Envelope envelope, Class<T> clazz) {
        return common.Jsons.MAPPER.convertValue(envelope.payload(), clazz);
    }

    private static class StepRuntime {
        final String planId;
        final int order;
        volatile String status;
        final String correlationId;

        private StepRuntime(String planId, int order, String status, String correlationId) {
            this.planId = planId;
            this.order = order;
            this.status = status;
            this.correlationId = correlationId;
        }
    }
}
